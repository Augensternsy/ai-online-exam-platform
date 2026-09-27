package com.mindskip.xzs.service;

import com.mindskip.xzs.domain.ExamViolationLog;

import java.util.List;

public interface ExamViolationLogService extends BaseService<ExamViolationLog> {
    List<ExamViolationLog> page(Integer userId, Integer examPaperId);
    
    int pageCount(Integer userId, Integer examPaperId);
    
    List<ExamViolationLog> getViolationLogsByExamPaperId(Integer examPaperId);
    
    int getViolationCountByUserIdAndExamPaperId(Integer userId, Integer examPaperId);
    
    void recordViolation(Integer userId, String userName, Integer examPaperId, String examPaperName, 
                         String violationType, String violationDetail, Integer violationCount);
    
    void handleViolation(Integer violationLogId, String handleResult);
}
