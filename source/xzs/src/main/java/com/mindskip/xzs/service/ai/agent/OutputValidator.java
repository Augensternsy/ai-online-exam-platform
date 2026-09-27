package com.mindskip.xzs.service.ai.agent;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结果验证模块：对 LLM 返回的试卷 JSON 进行结构、字段、答案、数量校验，
 * 只保留合法题目，并返回校验报告供 ExamAgent 决定是否重试。
 */
@Component
public class OutputValidator {

    private static final Logger logger = LoggerFactory.getLogger(OutputValidator.class);

    /**
     * 校验并抽取合法题目。
     *
     * @param rawResponse   LLM 原始输出
     * @param expectedCount 期望题目数量
     * @return 校验结果，包含合法题目列表与统计信息
     */
    public ValidationResult validate(String rawResponse, int expectedCount) {
        ValidationResult result = new ValidationResult();
        JsonNode root = JsonRepairUtil.parse(rawResponse);
        if (root == null) {
            result.setError("无法解析 LLM 返回的 JSON");
            return result;
        }

        JsonNode questionsNode = root.get("questions");
        if (questionsNode == null || !questionsNode.isArray()) {
            result.setError("返回 JSON 缺少 questions 数组");
            return result;
        }

        if (root.has("title")) {
            result.setTitle(root.get("title").asText());
        }

        for (JsonNode q : questionsNode) {
            Map<String, Object> item = validateQuestion(q);
            if (item != null) {
                result.getQuestions().add(item);
            } else {
                result.incrementRejected();
            }
        }

        result.setTotal(questionsNode.size());
        logger.info("Validation: total={}, valid={}, rejected={}, expected={}",
                result.getTotal(), result.getQuestions().size(), result.getRejected(), expectedCount);
        return result;
    }

    private Map<String, Object> validateQuestion(JsonNode q) {
        if (q == null) return null;

        int type = JsonRepairUtil.intVal(q, "type", 1);
        if (type < 1 || type > 3) type = 1;

        String question = JsonRepairUtil.text(q, "question", "").trim();
        if (question.isEmpty()) return null;

        List<String> options = new ArrayList<>();
        JsonNode optNode = q.get("options");
        if (optNode != null && optNode.isArray()) {
            for (JsonNode o : optNode) options.add(o.asText());
        }

        // 选项数量校验
        int expectedOptions = (type == 3) ? 2 : 4;
        if (options.size() != expectedOptions) return null;

        // 兼容不同模型返回的答案字段名（answer / correct_answer）
        String answer = JsonRepairUtil.text(q, "answer", "");
        if (answer.isEmpty()) {
            answer = JsonRepairUtil.text(q, "correct_answer", "");
        }
        answer = answer.trim().toUpperCase();
        if (answer.isEmpty()) return null;

        // 答案与选项匹配校验
        if (!isAnswerValid(answer, type, options)) return null;

        String analysis = JsonRepairUtil.text(q, "analysis", "");
        String knowledgePoint = JsonRepairUtil.text(q, "knowledgePoint", "");
        if (knowledgePoint.isEmpty()) {
            knowledgePoint = JsonRepairUtil.text(q, "knowledge_point", "");
        }

        Map<String, Object> item = new HashMap<>();
        item.put("questionType", type);
        item.put("question", question);
        item.put("options", options);
        item.put("correct_answer", answer);
        item.put("analysis", analysis);
        item.put("knowledge_point", knowledgePoint);
        item.put("difficulty", JsonRepairUtil.intVal(q, "difficulty", 2));
        return item;
    }

    private boolean isAnswerValid(String answer, int type, List<String> options) {
        if (type == 1 || type == 3) {
            if (answer.length() != 1) return false;
            int idx = answer.charAt(0) - 'A';
            return idx >= 0 && idx < options.size();
        } else { // 多选
            String[] parts = answer.split(",");
            if (parts.length < 2) return false;
            for (String p : parts) {
                String a = p.trim();
                if (a.length() != 1) return false;
                int idx = a.charAt(0) - 'A';
                if (idx < 0 || idx >= options.size()) return false;
            }
            return true;
        }
    }

    public static class ValidationResult {
        private String title;
        private final List<Map<String, Object>> questions = new ArrayList<>();
        private int total;
        private int rejected;
        private String error;

        public boolean hasError() { return error != null; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public List<Map<String, Object>> getQuestions() { return questions; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getRejected() { return rejected; }
        public void incrementRejected() { this.rejected++; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
