package com.mindskip.xzs.viewmodel.question.recommend;

public class QuestionRecommendVO {
    
    private Integer questionId;
    private String questionContent;
    private Integer subjectId;
    private Integer difficulty;
    private Double recommendScore;
    private String recommendReason;

    public QuestionRecommendVO() {}

    public Integer getQuestionId() { return questionId; }
    public void setQuestionId(Integer questionId) { this.questionId = questionId; }
    
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    
    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    
    public Double getRecommendScore() { return recommendScore; }
    public void setRecommendScore(Double recommendScore) { this.recommendScore = recommendScore; }
    
    public String getRecommendReason() { return recommendReason; }
    public void setRecommendReason(String recommendReason) { this.recommendReason = recommendReason; }
}