package com.mindskip.ai.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/** Spring AI 结构化输出：题目列表包装（模型返回 { "questions": [...] }）。 */
public class GeneratedQuestionsResponse {

    @JsonPropertyDescription("题目列表")
    private List<GeneratedQuestion> questions;

    public List<GeneratedQuestion> getQuestions() { return questions; }
    public void setQuestions(List<GeneratedQuestion> questions) { this.questions = questions; }
}
