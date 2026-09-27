package com.mindskip.xzs.service.ai.agent;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 需求理解模块：将用户自然语言输入解析为结构化 ExamRequirement。
 * 优先使用 LLM 解析；LLM 失败时回退到规则解析。
 */
@Component
public class RequirementParser {

    private static final Logger logger = LoggerFactory.getLogger(RequirementParser.class);

    @Autowired
    private PromptPlanner promptPlanner;

    @Autowired
    private LLMExecutor llmExecutor;

    public ExamRequirement parse(String userInput) {
        ExamRequirement req = new ExamRequirement();
        req.setRawInput(userInput);

        try {
            String prompt = promptPlanner.buildRequirementParsePrompt(userInput);
            String response = llmExecutor.call(prompt);
            JsonNode root = JsonRepairUtil.parse(response);

            if (root != null) {
                req.setSubject(JsonRepairUtil.text(root, "subject", "通用"));
                req.setDifficulty(JsonRepairUtil.intVal(root, "difficulty", 2));
                req.setQuestionCount(JsonRepairUtil.intVal(root, "questionCount", 10));

                List<Integer> types = new ArrayList<>();
                JsonNode typesNode = root.get("questionTypes");
                if (typesNode != null && typesNode.isArray()) {
                    for (JsonNode t : typesNode) {
                        int code = t.asInt();
                        if (code >= 1 && code <= 3) types.add(code);
                    }
                }
                if (types.isEmpty()) types.add(1);
                req.setQuestionTypes(types);

                List<String> kps = new ArrayList<>();
                JsonNode kpNode = root.get("knowledgePoints");
                if (kpNode != null && kpNode.isArray()) {
                    for (JsonNode k : kpNode) kps.add(k.asText());
                }
                req.setKnowledgePoints(kps);

                logger.info("LLM parsed requirement: {}", req);
                return req;
            }
        } catch (Exception e) {
            logger.warn("LLM requirement parse failed, fallback to rule-based: {}", e.getMessage());
        }

        return ruleBasedParse(userInput, req);
    }

    /** 回退：基于关键词的规则解析，保证 LLM 不可用时仍可工作。 */
    private ExamRequirement ruleBasedParse(String input, ExamRequirement req) {
        String lower = input.toLowerCase();

        // 难度
        if (input.contains("简单") || lower.contains("easy")) req.setDifficulty(1);
        else if (input.contains("困难") || input.contains("高级") || lower.contains("hard")) req.setDifficulty(3);
        else req.setDifficulty(2);

        // 题型
        List<Integer> types = new ArrayList<>();
        if (input.contains("多选")) types.add(2);
        if (input.contains("判断")) types.add(3);
        if (input.contains("单选") || types.isEmpty()) types.add(1);
        if (input.contains("混合") || input.contains("综合")) {
            types.clear();
            types.add(1); types.add(2); types.add(3);
        }
        req.setQuestionTypes(types);

        // 数量：提取数字
        int count = 10;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)\\s*道").matcher(input);
        if (m.find()) count = Integer.parseInt(m.group(1));
        req.setQuestionCount(count);

        // 科目：取常见关键词
        String subject = "通用";
        for (String sub : new String[]{"Java", "Python", "MySQL", "Spring", "数据结构", "算法", "操作系统", "计算机网络", "C语言", "C++"}) {
            if (lower.contains(sub.toLowerCase())) { subject = sub; break; }
        }
        req.setSubject(subject);
        req.setKnowledgePoints(new ArrayList<>());

        logger.info("Rule-based parsed requirement: {}", req);
        return req;
    }
}
