package com.mindskip.xzs.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuestionRecommendMapper {

    /**
     * 获取题目详情（含题干内容）
     */
    Map<String, Object> getQuestionWithContent(@Param("questionId") Integer questionId);

    /**
     * 获取同一科目的候选题目（排除当前题目）
     */
    List<Map<String, Object>> getCandidateQuestions(
            @Param("subjectId") Integer subjectId,
            @Param("excludeQuestionId") Integer excludeQuestionId);

    /**
     * 获取学生错题列表（包含题目难度）
     */
    List<Map<String, Object>> getStudentWrongQuestions(@Param("studentId") Integer studentId);

    /**
     * 获取学生已答题列表
     */
    List<Integer> getStudentAnsweredQuestionIds(@Param("studentId") Integer studentId);

    /**
     * 获取科目下所有题目
     */
    List<Map<String, Object>> getQuestionsBySubject(@Param("subjectId") Integer subjectId);

    /**
     * 获取最近创建的题目（用于默认推荐）
     */
    List<Map<String, Object>> getRecentQuestions(@Param("limit") Integer limit);
}