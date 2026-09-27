package com.mindskip.xzs.controller.student;

import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.domain.ExamPaper;
import com.mindskip.xzs.domain.User;
import com.mindskip.xzs.service.ExamPaperAnswerService;
import com.mindskip.xzs.service.ExamPaperService;
import com.mindskip.xzs.service.ExamViolationLogService;
import com.mindskip.xzs.utility.DateTimeUtil;
import com.mindskip.xzs.utility.PageInfoHelper;
import com.mindskip.xzs.viewmodel.admin.exam.ExamPaperEditRequestVM;
import com.mindskip.xzs.viewmodel.student.exam.ExamPaperPageResponseVM;
import com.mindskip.xzs.viewmodel.student.exam.ExamPaperPageVM;
import com.mindskip.xzs.viewmodel.student.exam.ExamViolationRequestVM;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController("StudentExamPaperController")
@RequestMapping(value = "/api/student/exam/paper")
public class ExamPaperController extends BaseApiController {

    private final ExamPaperService examPaperService;
    private final ExamPaperAnswerService examPaperAnswerService;
    private final ApplicationEventPublisher eventPublisher;
    private final ExamViolationLogService examViolationLogService;

    @Autowired
    public ExamPaperController(ExamPaperService examPaperService, ExamPaperAnswerService examPaperAnswerService, 
                               ApplicationEventPublisher eventPublisher, ExamViolationLogService examViolationLogService) {
        this.examPaperService = examPaperService;
        this.examPaperAnswerService = examPaperAnswerService;
        this.eventPublisher = eventPublisher;
        this.examViolationLogService = examViolationLogService;
    }


    @RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
    public RestResponse<ExamPaperEditRequestVM> select(@PathVariable Integer id) {
        ExamPaperEditRequestVM vm = examPaperService.examPaperToVM(id);
        return RestResponse.ok(vm);
    }


    @RequestMapping(value = "/pageList", method = RequestMethod.POST)
    public RestResponse<PageInfo<ExamPaperPageResponseVM>> pageList(@RequestBody @Valid ExamPaperPageVM model) {
        PageInfo<ExamPaper> pageInfo = examPaperService.studentPage(model);
        PageInfo<ExamPaperPageResponseVM> page = PageInfoHelper.copyMap(pageInfo, e -> {
            ExamPaperPageResponseVM vm = modelMapper.map(e, ExamPaperPageResponseVM.class);
            vm.setCreateTime(DateTimeUtil.dateFormat(e.getCreateTime()));
            return vm;
        });
        return RestResponse.ok(page);
    }

    @RequestMapping(value = "/recordViolation", method = RequestMethod.POST)
    public RestResponse<Void> recordViolation(@RequestBody @Valid ExamViolationRequestVM model) {
        User user = getCurrentUser();
        examViolationLogService.recordViolation(
                user.getId(),
                user.getUserName(),
                model.getExamPaperId(),
                model.getExamPaperName(),
                model.getViolationType(),
                model.getViolationDetail(),
                model.getViolationCount()
        );
        return RestResponse.ok();
    }

    @RequestMapping(value = "/getViolationLogs/{examPaperId}", method = RequestMethod.POST)
    public RestResponse<java.util.List<com.mindskip.xzs.domain.ExamViolationLog>> getViolationLogs(@PathVariable Integer examPaperId) {
        java.util.List<com.mindskip.xzs.domain.ExamViolationLog> logs = examViolationLogService.getViolationLogsByExamPaperId(examPaperId);
        return RestResponse.ok(logs);
    }
}
