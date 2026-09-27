package com.mindskip.xzs.service.ai.agent;

import java.util.List;

/**
 * AI 出题需求解析结果。
 * 由 RequirementParser 从自然语言中提取，供 PromptPlanner 使用。
 */
public class ExamRequirement {

    /** 科目名称，如 "Java"、"Python" */
    private String subject;

    /** 难度：1 简单 / 2 中等 / 3 困难 */
    private Integer difficulty;

    /** 题目总数 */
    private Integer questionCount;

    /** 要求的题型列表：1 单选 / 2 多选 / 3 判断 */
    private List<Integer> questionTypes;

    /** 知识点列表（可选） */
    private List<String> knowledgePoints;

    /** 年级（可选） */
    private Integer gradeLevel;

    /** 原始用户输入 */
    private String rawInput;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }

    public Integer getQuestionCount() { return questionCount; }
    public void setQuestionCount(Integer questionCount) { this.questionCount = questionCount; }

    public List<Integer> getQuestionTypes() { return questionTypes; }
    public void setQuestionTypes(List<Integer> questionTypes) { this.questionTypes = questionTypes; }

    public List<String> getKnowledgePoints() { return knowledgePoints; }
    public void setKnowledgePoints(List<String> knowledgePoints) { this.knowledgePoints = knowledgePoints; }

    public Integer getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(Integer gradeLevel) { this.gradeLevel = gradeLevel; }

    public String getRawInput() { return rawInput; }
    public void setRawInput(String rawInput) { this.rawInput = rawInput; }

    @Override
    public String toString() {
        return "ExamRequirement{" +
                "subject='" + subject + '\'' +
                ", difficulty=" + difficulty +
                ", questionCount=" + questionCount +
                ", questionTypes=" + questionTypes +
                ", knowledgePoints=" + knowledgePoints +
                '}';
    }
}
