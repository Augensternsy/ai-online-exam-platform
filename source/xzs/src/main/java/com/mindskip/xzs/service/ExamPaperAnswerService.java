package com.mindskip.xzs.service;

import com.mindskip.xzs.domain.ExamPaperAnswer;
import com.mindskip.xzs.domain.ExamPaperAnswerInfo;
import com.mindskip.xzs.domain.User;
import com.mindskip.xzs.viewmodel.student.exam.ExamAnswerSaveVM;
import com.mindskip.xzs.viewmodel.student.exam.ExamPaperSubmitVM;
import com.mindskip.xzs.viewmodel.student.exampaper.ExamPaperAnswerPageVM;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ExamPaperAnswerService extends BaseService<ExamPaperAnswer> {

    PageInfo<ExamPaperAnswer> studentPage(ExamPaperAnswerPageVM requestVM);

    ExamPaperAnswerInfo calculateExamPaperAnswer(ExamPaperSubmitVM examPaperSubmitVM, User user);

    String judge(ExamPaperSubmitVM examPaperSubmitVM);

    ExamPaperSubmitVM examPaperAnswerToVM(Integer id);

    Integer selectAllCount();

    List<Integer> selectMothCount();

    PageInfo<ExamPaperAnswer> adminPage(com.mindskip.xzs.viewmodel.admin.paper.ExamPaperAnswerPageRequestVM requestVM);

    ExamPaperAnswer startExam(Integer examPaperId, User user);

    boolean saveAnswer(ExamAnswerSaveVM saveVM, User user);

    String submitExam(ExamPaperSubmitVM examPaperSubmitVM, User user);
}
