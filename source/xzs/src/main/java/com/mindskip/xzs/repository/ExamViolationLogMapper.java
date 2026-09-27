package com.mindskip.xzs.repository;

import com.mindskip.xzs.domain.ExamViolationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamViolationLogMapper extends BaseMapper<ExamViolationLog> {
    List<ExamViolationLog> page(@Param("userId") Integer userId, @Param("examPaperId") Integer examPaperId);
    
    int pageCount(@Param("userId") Integer userId, @Param("examPaperId") Integer examPaperId);
    
    List<ExamViolationLog> getViolationLogsByExamPaperId(@Param("examPaperId") Integer examPaperId);
    
    int getViolationCountByUserIdAndExamPaperId(@Param("userId") Integer userId, @Param("examPaperId") Integer examPaperId);
}
