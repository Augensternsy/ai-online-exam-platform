package com.mindskip.xzs.service;

import com.mindskip.xzs.viewmodel.question.recommend.QuestionRecommendVO;

import java.util.List;

public interface QuestionRecommendService {

    /**
     * 相似题检索
     * @param questionId 目标题目ID
     * @param topK 返回数量
     * @return 相似题列表
     */
    List<QuestionRecommendVO> findSimilarQuestions(Integer questionId, Integer topK);

    /**
     * 个性化练习题推荐
     * @param studentId 学生ID
     * @param topK 返回数量
     * @return 推荐练习题列表
     */
    List<QuestionRecommendVO> recommendPersonalized(Integer studentId, Integer topK);
}