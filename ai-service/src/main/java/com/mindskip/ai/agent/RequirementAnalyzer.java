package com.mindskip.ai.agent;

import com.mindskip.ai.dto.QuestionGenerateRequest;
import org.springframework.stereotype.Component;

/** 步骤一：需求解析 —— 将请求参数规整为标准化出题参数。 */
@Component
public class RequirementAnalyzer {

    public AnalyzedRequirement analyze(QuestionGenerateRequest req) {
        AnalyzedRequirement r = new AnalyzedRequirement();
        r.setSubject(req.getSubject() == null || req.getSubject().isBlank() ? "Java" : req.getSubject().trim());
        r.setDifficulty(normalizeDifficulty(req.getDifficulty()));
        r.setQuestionCount(req.getQuestionCount() == null || req.getQuestionCount() <= 0 ? 10 : req.getQuestionCount());
        r.setQuestionTypes(req.getQuestionTypes() == null || req.getQuestionTypes().isEmpty()
                ? java.util.List.of("single") : req.getQuestionTypes());
        r.setContent(req.getContent());
        r.setQuestionType(req.getQuestionType());
        return r;
    }

    private String normalizeDifficulty(String d) {
        if (d == null) return "medium";
        String lower = d.trim().toLowerCase();
        if (lower.equals("easy") || lower.equals("简单") || lower.equals("1")) return "easy";
        if (lower.equals("hard") || lower.equals("困难") || lower.equals("3")) return "hard";
        return "medium";
    }

    public static class AnalyzedRequirement {
        private String subject;
        private String difficulty;
        private int questionCount;
        private java.util.List<String> questionTypes;
        private String content;
        private Integer questionType;

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public int getQuestionCount() { return questionCount; }
        public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
        public java.util.List<String> getQuestionTypes() { return questionTypes; }
        public void setQuestionTypes(java.util.List<String> questionTypes) { this.questionTypes = questionTypes; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Integer getQuestionType() { return questionType; }
        public void setQuestionType(Integer questionType) { this.questionType = questionType; }
    }
}
