package com.mindskip.xzs.viewmodel.student.exam;

import javax.validation.constraints.NotNull;

public class ExamStartVM {

    @NotNull
    private Integer examPaperId;

    public Integer getExamPaperId() {
        return examPaperId;
    }

    public void setExamPaperId(Integer examPaperId) {
        this.examPaperId = examPaperId;
    }
}
