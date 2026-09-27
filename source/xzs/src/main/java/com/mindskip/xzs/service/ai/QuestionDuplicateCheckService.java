package com.mindskip.xzs.service.ai;

import com.mindskip.xzs.repository.QuestionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题库查重服务 - 用于检查生成的题目是否与已有题目重复
 */
@Service
public class QuestionDuplicateCheckService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionDuplicateCheckService.class);

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 检查题目是否重复
     * @param keywords 关键词（如知识点、题干关键词）
     * @param subjectId 学科ID（可选）
     * @return 查重结果，包含相似度和已有题目信息
     */
    public Map<String, Object> checkDuplicate(String keywords, Integer subjectId) {
        logger.info("Checking duplicate questions for keywords: {}, subjectId: {}", keywords, subjectId);

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> similarQuestions = questionMapper.searchSimilarQuestions(keywords, subjectId);
        
        result.put("hasDuplicate", !similarQuestions.isEmpty());
        result.put("similarQuestions", similarQuestions);
        result.put("duplicateCount", similarQuestions.size());
        
        if (!similarQuestions.isEmpty()) {
            logger.warn("Found {} similar questions for keywords: {}", similarQuestions.size(), keywords);
        }
        
        return result;
    }

    /**
     * 检查单个题目是否与题库重复
     * @param questionText 题干文本
     * @param subjectId 学科ID（可选）
     * @param threshold 相似度阈值（0-1）
     * @return 是否重复
     */
    public boolean isDuplicate(String questionText, Integer subjectId, double threshold) {
        Map<String, Object> result = checkDuplicate(questionText, subjectId);
        List<Map<String, Object>> similarQuestions = (List<Map<String, Object>>) result.get("similarQuestions");
        
        for (Map<String, Object> question : similarQuestions) {
            Double similarity = (Double) question.get("similarity");
            if (similarity != null && similarity >= threshold) {
                return true;
            }
        }
        return false;
    }
}