package com.mindskip.xzs.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindskip.xzs.domain.ExamPaperAnswer;
import com.mindskip.xzs.domain.ExamPaperQuestionCustomerAnswer;
import com.mindskip.xzs.domain.Question;
import com.mindskip.xzs.domain.TextContent;
import com.mindskip.xzs.repository.ExamPaperAnswerMapper;
import com.mindskip.xzs.repository.QuestionMapper;
import com.mindskip.xzs.repository.TextContentMapper;
import com.mindskip.xzs.service.ExamPaperQuestionCustomerAnswerService;
import com.mindskip.xzs.service.ai.agent.JsonRepairUtil;
import com.mindskip.xzs.service.ai.agent.LLMExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 智能评测服务：基于学生答题数据，调用 LLM 生成成绩分析、薄弱知识点与学习建议。
 */
@Service
public class ExamEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(ExamEvaluationService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ExamPaperAnswerMapper examPaperAnswerMapper;

    @Autowired
    private ExamPaperQuestionCustomerAnswerService questionAnswerService;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private TextContentMapper textContentMapper;

    @Autowired
    private LLMExecutor llmExecutor;

    /**
     * 对指定答卷进行 AI 智能评测。
     *
     * @param examPaperAnswerId 答卷 ID
     * @return { score, correctRate, weakPoints, suggestion, questionStats }
     */
    public Map<String, Object> evaluate(Integer examPaperAnswerId) throws Exception {
        ExamPaperAnswer answer = examPaperAnswerMapper.selectByPrimaryKey(examPaperAnswerId);
        if (answer == null) {
            throw new IllegalArgumentException("答卷不存在: " + examPaperAnswerId);
        }

        List<ExamPaperQuestionCustomerAnswer> items = questionAnswerService.selectListByPaperAnswerId(examPaperAnswerId);

        int total = items.size();
        int correct = 0;
        int wrong = 0;
        List<Map<String, Object>> wrongQuestions = new ArrayList<>();

        for (ExamPaperQuestionCustomerAnswer item : items) {
            boolean right = Boolean.TRUE.equals(item.getDoRight());
            if (right) {
                correct++;
            } else {
                wrong++;
                wrongQuestions.add(buildWrongQuestionSummary(item));
            }
        }

        int systemScore = answer.getSystemScore() != null ? answer.getSystemScore() : 0;
        int paperScore = answer.getPaperScore() != null ? answer.getPaperScore() : 100;
        double correctRate = total > 0 ? (correct * 100.0 / total) : 0.0;

        // 调用 LLM 生成分析
        String prompt = buildEvaluationPrompt(answer.getPaperName(), systemScore, paperScore,
                correct, total, wrongQuestions);

        Map<String, Object> aiResult;
        try {
            String response = llmExecutor.call(prompt);
            aiResult = parseEvaluationResponse(response);
        } catch (Exception e) {
            logger.error("AI evaluation failed, fallback to static analysis", e);
            aiResult = staticAnalysis(systemScore, correctRate, wrongQuestions);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("paperName", answer.getPaperName());
        result.put("score", systemScore);
        result.put("paperScore", paperScore);
        result.put("correctCount", correct);
        result.put("totalCount", total);
        result.put("wrongCount", wrong);
        result.put("correctRate", Math.round(correctRate * 10) / 10.0);
        result.put("weakPoints", aiResult.get("weakPoints"));
        result.put("suggestion", aiResult.get("suggestion"));
        result.put("knowledgeStats", buildKnowledgeStats(items));

        logger.info("Exam evaluation done: paper={}, score={}, correctRate={}",
                answer.getPaperName(), systemScore, correctRate);
        return result;
    }

    private Map<String, Object> buildWrongQuestionSummary(ExamPaperQuestionCustomerAnswer item) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("questionType", item.getQuestionType());
        m.put("userAnswer", item.getAnswer());
        m.put("score", item.getCustomerScore());
        m.put("fullScore", item.getQuestionScore());

        // 提取题干与解析（用于 LLM 判断薄弱知识点）
        try {
            Question q = questionMapper.selectByPrimaryKey(item.getQuestionId());
            if (q != null && q.getInfoTextContentId() != null) {
                TextContent tc = textContentMapper.selectByPrimaryKey(q.getInfoTextContentId());
                if (tc != null) {
                    JsonNode node = objectMapper.readTree(tc.getContent());
                    m.put("question", node.path("titleContent").asText(""));
                    m.put("analysis", node.path("analyze").asText(""));
                }
            }
        } catch (Exception e) {
            logger.debug("load question detail failed: {}", e.getMessage());
        }
        return m;
    }

    private String buildEvaluationPrompt(String paperName, int score, int paperScore,
                                          int correct, int total, List<Map<String, Object>> wrongQuestions) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位资深的学习辅导老师。请根据以下学生答卷数据，生成一份简洁的成绩分析报告。\n\n");
        sb.append("## 答卷信息\n");
        sb.append("- 试卷名称：").append(paperName).append("\n");
        sb.append("- 得分：").append(score).append(" / ").append(paperScore).append("\n");
        sb.append("- 正确题数：").append(correct).append(" / ").append(total).append("\n");

        if (!wrongQuestions.isEmpty()) {
            sb.append("\n## 答错的题目\n");
            int idx = 1;
            for (Map<String, Object> wq : wrongQuestions) {
                sb.append(idx++).append(". ").append(wq.getOrDefault("question", ""));
                sb.append("（学生答案：").append(wq.getOrDefault("userAnswer", "空"));
                sb.append("，解析：").append(wq.getOrDefault("analysis", "")).append("）\n");
                if (idx > 15) break; // 控制长度
            }
        }

        sb.append("\n## 输出要求\n");
        sb.append("请严格按以下 JSON 格式返回，不要输出额外内容：\n");
        sb.append("```json\n");
        sb.append("{\n");
        sb.append("  \"weakPoints\": [\"薄弱知识点1\", \"薄弱知识点2\"],\n");
        sb.append("  \"suggestion\": \"针对该学生的学习建议，200字以内\"\n");
        sb.append("}\n");
        sb.append("```\n\n");
        sb.append("要求：weakPoints 从错题解析中归纳 2-4 个具体知识点；suggestion 要具体、可执行。");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseEvaluationResponse(String response) {
        Map<String, Object> result = new HashMap<>();
        JsonNode root = JsonRepairUtil.parse(response);
        List<String> weakPoints = new ArrayList<>();
        if (root != null) {
            JsonNode wp = root.get("weakPoints");
            if (wp != null && wp.isArray()) {
                for (JsonNode n : wp) weakPoints.add(n.asText());
            }
            result.put("suggestion", JsonRepairUtil.text(root, "suggestion", "继续努力，针对错题知识点多加练习。"));
        } else {
            result.put("suggestion", "继续努力，针对错题知识点多加练习。");
        }
        if (weakPoints.isEmpty()) weakPoints.add("综合知识运用");
        result.put("weakPoints", weakPoints);
        return result;
    }

    private Map<String, Object> staticAnalysis(int score, double correctRate, List<Map<String, Object>> wrongQuestions) {
        Map<String, Object> m = new HashMap<>();
        List<String> wp = new ArrayList<>();
        for (Map<String, Object> wq : wrongQuestions) {
            String analysis = String.valueOf(wq.getOrDefault("analysis", ""));
            if (!analysis.isEmpty() && analysis.length() < 20) wp.add(analysis);
        }
        if (wp.isEmpty()) wp.add("综合知识运用");
        m.put("weakPoints", wp);
        m.put("suggestion", correctRate >= 80 ? "表现优秀，建议挑战更高难度题目。"
                : correctRate >= 60 ? "基础尚可，需针对错题知识点加强练习。"
                : "基础薄弱，建议系统复习核心知识点后再刷题。");
        return m;
    }

    /** 按题型统计正确率，供前端图表展示。 */
    private List<Map<String, Object>> buildKnowledgeStats(List<ExamPaperQuestionCustomerAnswer> items) {
        Map<Integer, int[]> stat = new LinkedHashMap<>(); // type -> [correct, total]
        for (ExamPaperQuestionCustomerAnswer item : items) {
            int type = item.getQuestionType() != null ? item.getQuestionType() : 1;
            int[] arr = stat.computeIfAbsent(type, k -> new int[2]);
            arr[1]++;
            if (Boolean.TRUE.equals(item.getDoRight())) arr[0]++;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<Integer, int[]> e : stat.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("type", e.getKey());
            m.put("typeName", typeName(e.getKey()));
            m.put("correct", e.getValue()[0]);
            m.put("total", e.getValue()[1]);
            m.put("rate", e.getValue()[1] > 0
                    ? Math.round(e.getValue()[0] * 1000.0 / e.getValue()[1]) / 10.0 : 0);
            list.add(m);
        }
        return list;
    }

    private String typeName(int t) {
        switch (t) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            case 5: return "简答题";
            default: return "其他";
        }
    }
}
