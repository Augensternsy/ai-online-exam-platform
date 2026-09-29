package com.mindskip.ai.dto;

/** LLM-as-a-Judge 质量评估结果。 */
public class QualityEvaluation {

    private double overall_score;
    private double factuality_score;
    private double relevance_score;
    private double difficulty_alignment_score;
    private String feedback;

    public double getOverall_score() { return overall_score; }
    public void setOverall_score(double overall_score) { this.overall_score = overall_score; }
    public double getFactuality_score() { return factuality_score; }
    public void setFactuality_score(double factuality_score) { this.factuality_score = factuality_score; }
    public double getRelevance_score() { return relevance_score; }
    public void setRelevance_score(double relevance_score) { this.relevance_score = relevance_score; }
    public double getDifficulty_alignment_score() { return difficulty_alignment_score; }
    public void setDifficulty_alignment_score(double difficulty_alignment_score) { this.difficulty_alignment_score = difficulty_alignment_score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    /** 综合得分：优先 overall_score，否则取三项均值 */
    public double effectiveScore() {
        if (overall_score > 0) {
            return overall_score;
        }
        return (factuality_score + relevance_score + difficulty_alignment_score) / 3.0;
    }
}
