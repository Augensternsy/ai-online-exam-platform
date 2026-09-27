package com.mindskip.xzs.controller.admin;

import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.service.QuestionRecommendService;
import com.mindskip.xzs.viewmodel.question.recommend.QuestionRecommendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/question/recommend")
public class QuestionRecommendController {

    @Autowired
    private QuestionRecommendService questionRecommendService;

    /**
     * 相似题推荐接口
     * GET /api/question/recommend/similar?questionId={questionId}&topK={topK}
     */
    @GetMapping("/similar")
    public RestResponse<List<QuestionRecommendVO>> getSimilarQuestions(
            @RequestParam Integer questionId,
            @RequestParam(required = false) Integer topK) {
        
        List<QuestionRecommendVO> result = questionRecommendService.findSimilarQuestions(questionId, topK);
        return RestResponse.ok(result);
    }

    /**
     * 个性化推荐接口
     * GET /api/question/recommend/personalized?studentId={studentId}&topK={topK}
     */
    @GetMapping("/personalized")
    public RestResponse<List<QuestionRecommendVO>> getPersonalizedRecommendations(
            @RequestParam Integer studentId,
            @RequestParam(required = false) Integer topK) {
        
        List<QuestionRecommendVO> result = questionRecommendService.recommendPersonalized(studentId, topK);
        return RestResponse.ok(result);
    }
}