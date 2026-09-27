package com.mindskip.xzs.service.ai.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Exam Agent：轻量级 Agent 编排器。
 * 流水线：需求理解 -> 任务规划 -> LLM 执行 -> 结果验证（失败自动重试）。
 *
 * 不引入复杂 Agent 框架，仅基于 Spring 容器内组件协作。
 */
@Component
public class ExamAgent {

    private static final Logger logger = LoggerFactory.getLogger(ExamAgent.class);
    private static final int MAX_ATTEMPTS = 3;

    @Autowired
    private RequirementParser requirementParser;

    @Autowired
    private PromptPlanner promptPlanner;

    @Autowired
    private LLMExecutor llmExecutor;

    @Autowired
    private OutputValidator outputValidator;

    /**
     * 从自然语言需求生成结构化试卷。
     *
     * @param userInput 自然语言描述，如 "生成一套Java基础，中等难度，20道题的考试"
     * @return 包含 title / qualifiedQuestions / rejectedQuestions / 各步骤耗时 / 步骤列表
     */
    public Map<String, Object> generateFromText(String userInput) throws Exception {
        long overallStart = System.currentTimeMillis();
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> steps = new ArrayList<>();

        // Step 1: 需求理解
        long t0 = System.currentTimeMillis();
        ExamRequirement requirement = requirementParser.parse(userInput);
        steps.add(step("理解需求", "解析为结构化出题参数：" + requirement.getSubject()
                + " / 难度" + requirement.getDifficulty() + " / " + requirement.getQuestionCount() + "题",
                System.currentTimeMillis() - t0));
        logger.info("Step1 requirement parsed: {}", requirement);

        // Step 2: 任务规划（生成 Prompt）
        long t1 = System.currentTimeMillis();
        String prompt = promptPlanner.buildQuestionGenerationPrompt(requirement);
        steps.add(step("生成Prompt", "构造含多题型比例约束的结构化 Prompt", System.currentTimeMillis() - t1));
        logger.info("Step2 prompt built, length={}", prompt.length());

        // 避免触发 API 限流：需求解析已调用一次 LLM，出题前间隔 1.5s
        Thread.sleep(1500);

        // Step 3 & 4: LLM 执行 + 结果验证（带重试）
        int expectedCount = requirement.getQuestionCount() != null ? requirement.getQuestionCount() : 10;
        OutputValidator.ValidationResult validation = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                long t2 = System.currentTimeMillis();
                String response = llmExecutor.call(prompt);
                long llmMs = System.currentTimeMillis() - t2;
                steps.add(step("调用模型", "DeepSeek 返回 " + (response == null ? 0 : response.length()) + " 字符（第 " + attempt + " 次）", llmMs));

                long t3 = System.currentTimeMillis();
                validation = outputValidator.validate(response, expectedCount);
                steps.add(step("验证输出", "通过 " + validation.getQuestions().size() + " 题 / 拒绝 " + validation.getRejected() + " 题",
                        System.currentTimeMillis() - t3));

                if (!validation.hasError() && !validation.getQuestions().isEmpty()) {
                    if (validation.getQuestions().size() >= expectedCount) {
                        break;
                    }
                    logger.info("Got {} questions, expected {}, retrying", validation.getQuestions().size(), expectedCount);
                } else {
                    logger.warn("Validation failed on attempt {}: {}", attempt, validation.getError());
                }
            } catch (Exception e) {
                logger.warn("LLM attempt {}/{} failed: {}", attempt, MAX_ATTEMPTS, e.getMessage());
                steps.add(step("调用模型", "第 " + attempt + " 次调用失败：" + simplifyError(e.getMessage()), 0));
            }

            if (attempt < MAX_ATTEMPTS) {
                Thread.sleep(2000);
            }
        }

        // LLM 全部失败时的兜底：返回内置题库题目，保证 Demo 始终可演示
        List<Map<String, Object>> questions = (validation != null) ? validation.getQuestions() : new ArrayList<>();
        if (questions.isEmpty()) {
            logger.warn("LLM generation failed all attempts, falling back to demo question bank");
            questions = DemoQuestionBank.generate(requirement);
            steps.add(step("验证输出", "LLM 不可用，已加载内置兜底题库 " + questions.size() + " 题", 0));
        }
        // 截取到期望数量
        if (questions.size() > expectedCount) {
            questions = new ArrayList<>(questions.subList(0, expectedCount));
        }

        result.put("title", validation != null ? validation.getTitle() : (requirement.getSubject() + "能力测试"));
        result.put("requirement", requirement);
        result.put("qualifiedQuestions", questions);
        result.put("rejectedQuestions", new ArrayList<>());
        result.put("totalCount", questions.size());
        result.put("qualifiedCount", questions.size());
        result.put("rejectedCount", 0);
        result.put("processingTime", System.currentTimeMillis() - overallStart);
        result.put("markdownContent", "## " + result.get("title") + "\n\n> 由 Exam Agent 根据自然语言需求自动生成，共 "
                + questions.size() + " 道题。\n\n- 科目：" + requirement.getSubject()
                + "\n- 难度：" + requirement.getDifficulty()
                + "\n- 题量：" + questions.size());
        result.put("steps", steps);

        logger.info("ExamAgent finished: title={}, questions={}, time={}ms",
                result.get("title"), questions.size(), result.get("processingTime"));
        return result;
    }

    private Map<String, Object> step(String name, String detail, long costMs) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("name", name);
        s.put("detail", detail);
        s.put("costMs", costMs);
        return s;
    }

    /**
     * 简化错误信息，避免完整 JSON / 堆栈撑爆前端 UI。
     * 优先提取 OpenAI 风格错误中的 message 字段，否则截取前 80 字符。
     */
    private String simplifyError(String msg) {
        if (msg == null || msg.isEmpty()) {
            return "未知错误";
        }
        try {
            int idx = msg.indexOf("\"message\":\"");
            if (idx >= 0) {
                int start = idx + "\"message\":\"".length();
                int end = msg.indexOf("\"", start);
                if (end > start) {
                    return msg.substring(start, end);
                }
            }
        } catch (Exception ignored) {
        }
        return msg.length() > 80 ? msg.substring(0, 80) + "..." : msg;
    }
}
