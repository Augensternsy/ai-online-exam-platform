package com.mindskip.ai.service;

import com.mindskip.ai.agent.QualityEvaluator;
import com.mindskip.ai.agent.QuestionGenerator;
import com.mindskip.ai.agent.QuestionPlanner;
import com.mindskip.ai.agent.RequirementAnalyzer;
import com.mindskip.ai.dto.GeneratedQuestion;
import com.mindskip.ai.dto.QuestionGenerateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 编排 Agent 四步流程：需求解析 -> 规划 -> 生成 -> 校验评估。 */
@Service
public class QuestionGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionGenerationService.class);

    private final RequirementAnalyzer requirementAnalyzer;
    private final QuestionPlanner questionPlanner;
    private final QuestionGenerator questionGenerator;
    private final QualityEvaluator qualityEvaluator;
    private final LlmClient llmClient;

    public QuestionGenerationService(RequirementAnalyzer requirementAnalyzer,
                                     QuestionPlanner questionPlanner,
                                     QuestionGenerator questionGenerator,
                                     QualityEvaluator qualityEvaluator,
                                     LlmClient llmClient) {
        this.requirementAnalyzer = requirementAnalyzer;
        this.questionPlanner = questionPlanner;
        this.questionGenerator = questionGenerator;
        this.qualityEvaluator = qualityEvaluator;
        this.llmClient = llmClient;
    }

    public Map<String, Object> generate(QuestionGenerateRequest request) {
        long overallStart = System.currentTimeMillis();
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> steps = new ArrayList<>();

        // Step 1: 需求解析
        long t0 = System.currentTimeMillis();
        RequirementAnalyzer.AnalyzedRequirement req = requirementAnalyzer.analyze(request);
        steps.add(step("需求解析", req.getSubject() + " / " + req.getDifficulty() + " / " + req.getQuestionCount() + "题",
                System.currentTimeMillis() - t0));

        // Step 2: 规划（构建 Prompt）
        long t1 = System.currentTimeMillis();
        String prompt = questionPlanner.buildPrompt(req);
        steps.add(step("任务规划", "构造结构化出题 Prompt（长度 " + prompt.length() + "）", System.currentTimeMillis() - t1));

        // Step 3: 执行生成
        long t2 = System.currentTimeMillis();
        List<GeneratedQuestion> generated = questionGenerator.generate(prompt);
        steps.add(step("执行生成", "生成 " + generated.size() + " 道合法题目", System.currentTimeMillis() - t2));

        // Step 4: 校验评估（STANDARD）或跳过（FAST）
        List<GeneratedQuestion> qualified = new ArrayList<>();
        List<GeneratedQuestion> rejected = new ArrayList<>();
        List<GeneratedQuestion> evaluationFailed = new ArrayList<>();

        if (request.isFast()) {
            long t3 = System.currentTimeMillis();
            for (GeneratedQuestion q : generated) {
                q.setQualityScore(5.0);
                q.setStatus("qualified");
                qualified.add(q);
            }
            steps.add(step("校验评估", "FAST 模式跳过质量评估，全部标记合格", System.currentTimeMillis() - t3));
        } else {
            long t3 = System.currentTimeMillis();
            QualityEvaluator.EvaluationResult eval = qualityEvaluator.evaluate(req.getContent(), generated);
            qualified = eval.qualified;
            rejected = eval.rejected;
            evaluationFailed = eval.evaluationFailed;
            steps.add(step("校验评估", "合格 " + qualified.size() + " / 淘汰 " + rejected.size()
                    + " / 评估失败 " + evaluationFailed.size(), System.currentTimeMillis() - t3));
        }

        result.put("steps", steps);
        result.put("qualifiedQuestions", qualified);
        result.put("rejectedQuestions", rejected);
        result.put("evaluationFailedQuestions", evaluationFailed);
        result.put("totalCount", generated.size());
        result.put("qualifiedCount", qualified.size());
        result.put("rejectedCount", rejected.size());
        result.put("evaluationFailedCount", evaluationFailed.size());
        result.put("processingTime", System.currentTimeMillis() - overallStart);
        result.put("mode", request.getMode());
        result.put("title", req.getSubject() + "能力测试");

        logger.info("Generation finished: total={}, qualified={}, rejected={}, evalFailed={}, mode={}, time={}ms",
                generated.size(), qualified.size(), rejected.size(), evaluationFailed.size(),
                request.getMode(), result.get("processingTime"));
        return result;
    }

    private Map<String, Object> step(String name, String detail, long costMs) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("name", name);
        s.put("detail", detail);
        s.put("costMs", costMs);
        return s;
    }
}
