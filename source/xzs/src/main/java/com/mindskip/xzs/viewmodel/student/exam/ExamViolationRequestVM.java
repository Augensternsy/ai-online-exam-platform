package com.mindskip.xzs.viewmodel.student.exam;

public class ExamViolationRequestVM {
    private Integer examPaperId;
    private String examPaperName;
    private String violationType;
    private String violationDetail;
    private Integer violationCount;

    public Integer getExamPaperId() {
        return examPaperId;
    }

    public void setExamPaperId(Integer examPaperId) {
        this.examPaperId = examPaperId;
    }

    public String getExamPaperName() {
        return examPaperName;
    }

    public void setExamPaperName(String examPaperName) {
        this.examPaperName = examPaperName;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public String getViolationDetail() {
        return violationDetail;
    }

    public void setViolationDetail(String violationDetail) {
        this.violationDetail = violationDetail;
    }

    public Integer getViolationCount() {
        return violationCount;
    }

    public void setViolationCount(Integer violationCount) {
        this.violationCount = violationCount;
    }
}
