package com.mindskip.ai.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * 单道 AI 生成题目（Spring AI 结构化输出载体）。
 * 与主系统 Vue 页面兼容：question/options/correct_answer/analysis/difficulty。
 */
public class GeneratedQuestion {

    @JsonPropertyDescription("题目内容")
    private String question;

    @JsonPropertyDescription("选项列表，至少 2 个")
    private List<String> options;

    @JsonPropertyDescription("正确答案，如 A 或 A,B")
    private String correct_answer;

    @JsonPropertyDescription("题目解析")
    private String analysis;

    @JsonPropertyDescription("难度 1-3：1简单 2中等 3困难")
    private Integer difficulty;

    @JsonPropertyDescription("知识点")
    private String knowledge_point;

    // transient fields for evaluation pipeline (not from LLM)
    private transient Double qualityScore;
    private transient String status; // qualified | rejected | eval_failed

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public String getCorrect_answer() { return correct_answer; }
    public void setCorrect_answer(String correct_answer) { this.correct_answer = correct_answer; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    public String getKnowledge_point() { return knowledge_point; }
    public void setKnowledge_point(String knowledge_point) { this.knowledge_point = knowledge_point; }
    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
