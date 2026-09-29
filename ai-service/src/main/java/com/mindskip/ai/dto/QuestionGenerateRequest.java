package com.mindskip.ai.dto;

import java.util.List;

/** POST /api/ai/questions/generate 请求体。 */
public class QuestionGenerateRequest {

    private String subject;
    private String difficulty; // easy | medium | hard
    private Integer questionCount;
    private List<String> questionTypes; // single | multiple | judge
    /** FAST: 只生成不评估；STANDARD: 生成+质量评估 */
    private String mode = "STANDARD";
    /** 可选：教材/知识背景文本（PDF 已转 Markdown 后由主系统传入） */
    private String content;
    private Integer questionType; // 兼容主系统数字题型：1单选 2多选 3判断

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }
    public List<String> getQuestionTypes() { return questionTypes; }
    public void setQuestionTypes(List<String> questionTypes) { this.questionTypes = questionTypes; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getQuestionType() { return questionType; }
    public void setQuestionType(Integer questionType) { this.questionType = questionType; }

    public boolean isFast() {
        return "FAST".equalsIgnoreCase(mode);
    }
}
