package com.mindskip.xzs.viewmodel.student.exam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class ExamAnswerSaveVM {

    @NotNull
    private Integer examPaperId;

    @NotNull
    private Integer doTime;

    @NotNull
    @Valid
    private List<ExamPaperSubmitItemVM> answerItems;

    public Integer getExamPaperId() {
        return examPaperId;
    }

    public void setExamPaperId(Integer examPaperId) {
        this.examPaperId = examPaperId;
    }

    public Integer getDoTime() {
        return doTime;
    }

    public void setDoTime(Integer doTime) {
        this.doTime = doTime;
    }

    public List<ExamPaperSubmitItemVM> getAnswerItems() {
        return answerItems;
    }

    public void setAnswerItems(List<ExamPaperSubmitItemVM> answerItems) {
        this.answerItems = answerItems;
    }
}
