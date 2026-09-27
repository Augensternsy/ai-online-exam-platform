package com.mindskip.xzs.service.impl;

import com.mindskip.xzs.domain.ExamViolationLog;
import com.mindskip.xzs.repository.ExamViolationLogMapper;
import com.mindskip.xzs.service.ExamViolationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ExamViolationLogServiceImpl extends BaseServiceImpl<ExamViolationLog> implements ExamViolationLogService {

    private final ExamViolationLogMapper examViolationLogMapper;

    @Autowired
    public ExamViolationLogServiceImpl(ExamViolationLogMapper examViolationLogMapper) {
        super(examViolationLogMapper);
        this.examViolationLogMapper = examViolationLogMapper;
    }

    @Override
    public List<ExamViolationLog> page(Integer userId, Integer examPaperId) {
        return examViolationLogMapper.page(userId, examPaperId);
    }

    @Override
    public int pageCount(Integer userId, Integer examPaperId) {
        return examViolationLogMapper.pageCount(userId, examPaperId);
    }

    @Override
    public List<ExamViolationLog> getViolationLogsByExamPaperId(Integer examPaperId) {
        return examViolationLogMapper.getViolationLogsByExamPaperId(examPaperId);
    }

    @Override
    public int getViolationCountByUserIdAndExamPaperId(Integer userId, Integer examPaperId) {
        return examViolationLogMapper.getViolationCountByUserIdAndExamPaperId(userId, examPaperId);
    }

    @Override
    public void recordViolation(Integer userId, String userName, Integer examPaperId, String examPaperName,
                                String violationType, String violationDetail, Integer violationCount) {
        ExamViolationLog log = new ExamViolationLog();
        log.setUserId(userId);
        log.setUserName(userName);
        log.setExamPaperId(examPaperId);
        log.setExamPaperName(examPaperName);
        log.setViolationType(violationType);
        log.setViolationDetail(violationDetail);
        log.setViolationCount(violationCount);
        log.setViolationTime(new Date());
        log.setIsHandled(false);
        examViolationLogMapper.insert(log);
    }

    @Override
    public void handleViolation(Integer violationLogId, String handleResult) {
        ExamViolationLog log = examViolationLogMapper.selectByPrimaryKey(violationLogId);
        if (log != null) {
            log.setIsHandled(true);
            log.setHandleResult(handleResult);
            log.setHandleTime(new Date());
            examViolationLogMapper.updateByPrimaryKey(log);
        }
    }
}
