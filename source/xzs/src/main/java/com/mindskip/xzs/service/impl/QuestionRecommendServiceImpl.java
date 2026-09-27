package com.mindskip.xzs.service.impl;

import com.mindskip.xzs.repository.QuestionRecommendMapper;
import com.mindskip.xzs.service.QuestionRecommendService;
import com.mindskip.xzs.utility.TextSimilarityUtil;
import com.mindskip.xzs.viewmodel.question.recommend.QuestionRecommendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionRecommendServiceImpl implements QuestionRecommendService {

    @Autowired
    private QuestionRecommendMapper questionRecommendMapper;

    private static final int DEFAULT_TOP_K = 10;
    private static final int MAX_TOP_K = 50;

    @Override
    public List<QuestionRecommendVO> findSimilarQuestions(Integer questionId, Integer topK) {
        if (questionId == null || questionId <= 0) {
            return Collections.emptyList();
        }
        topK = validateTopK(topK);

        Map<String, Object> targetQuestion = questionRecommendMapper.getQuestionWithContent(questionId);
        if (targetQuestion == null) {
            return Collections.emptyList();
        }

        Integer subjectId = TextSimilarityUtil.toInteger(targetQuestion.get("subjectId"));
        String targetContent = (String) targetQuestion.get("questionContent");
        
        if (subjectId == null || targetContent == null || targetContent.isEmpty()) {
            return Collections.emptyList();
        }

        String targetText = TextSimilarityUtil.extractQuestionText(targetContent);
        if (targetText.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> candidates = questionRecommendMapper.getCandidateQuestions(subjectId, questionId);

        List<QuestionRecommendVO> results = new ArrayList<>();
        for (Map<String, Object> candidate : candidates) {
            Integer candidateId = TextSimilarityUtil.toInteger(candidate.get("questionId"));
            String candidateContent = (String) candidate.get("questionContent");
            
            if (candidateId == null || candidateContent == null || candidateContent.isEmpty()) {
                continue;
            }

            String candidateText = TextSimilarityUtil.extractQuestionText(candidateContent);
            if (candidateText.isEmpty()) {
                continue;
            }

            double similarity = TextSimilarityUtil.cosineSimilarity(targetText, candidateText);
            
            QuestionRecommendVO vo = createRecommendVO(candidate);
            double roundedScore = TextSimilarityUtil.roundToTwoDecimals(similarity);
            vo.setRecommendScore(roundedScore);
            String reason = roundedScore >= 0.8 ? "题干语义高度相似" : 
                           roundedScore >= 0.5 ? "题干语义相似" : "存在语义关联";
            vo.setRecommendReason(reason);
            results.add(vo);
        }

        results.sort((a, b) -> Double.compare(b.getRecommendScore(), a.getRecommendScore()));
        return results.stream().limit(topK).collect(Collectors.toList());
    }

    @Override
    public List<QuestionRecommendVO> recommendPersonalized(Integer studentId, Integer topK) {
        if (studentId == null || studentId <= 0) {
            return Collections.emptyList();
        }
        topK = validateTopK(topK);

        List<Map<String, Object>> wrongQuestions = questionRecommendMapper.getStudentWrongQuestions(studentId);
        
        if (wrongQuestions.isEmpty()) {
            return getDefaultRecommendations(topK);
        }

        Set<Integer> answeredQuestionIds = new HashSet<>(
                questionRecommendMapper.getStudentAnsweredQuestionIds(studentId));

        Map<Integer, Integer> subjectCount = new HashMap<>();
        Map<Integer, Integer> difficultyCount = new HashMap<>();
        List<String> wrongTexts = new ArrayList<>();
        
        for (Map<String, Object> wrong : wrongQuestions) {
            Integer subjectId = TextSimilarityUtil.toInteger(wrong.get("subjectId"));
            Integer difficulty = TextSimilarityUtil.toInteger(wrong.get("difficulty"));
            String content = (String) wrong.get("questionContent");
            
            if (subjectId != null) {
                subjectCount.merge(subjectId, 1, Integer::sum);
            }
            if (difficulty != null) {
                difficultyCount.merge(difficulty, 1, Integer::sum);
            }
            if (content != null && !content.isEmpty()) {
                wrongTexts.add(TextSimilarityUtil.extractQuestionText(content));
            }
        }

        int mainSubjectId = subjectCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(1);

        List<Map<String, Object>> candidates = questionRecommendMapper.getQuestionsBySubject(mainSubjectId);

        List<QuestionRecommendVO> results = new ArrayList<>();
        for (Map<String, Object> candidate : candidates) {
            Integer questionId = TextSimilarityUtil.toInteger(candidate.get("questionId"));
            
            if (questionId != null && answeredQuestionIds.contains(questionId)) {
                continue;
            }

            String candidateContent = (String) candidate.get("questionContent");
            Integer candidateDifficulty = TextSimilarityUtil.toInteger(candidate.get("difficulty"));
            
            if (candidateContent == null || candidateContent.isEmpty()) {
                continue;
            }

            String candidateText = TextSimilarityUtil.extractQuestionText(candidateContent);
            if (candidateText.isEmpty()) {
                continue;
            }

            Integer candidateSubjectId = TextSimilarityUtil.toInteger(candidate.get("subjectId"));
            double subjectScore = calculateSubjectScore(candidateSubjectId, subjectCount);
            double similarityScore = calculateSimilarityScore(candidateText, wrongTexts);
            double difficultyScore = calculateDifficultyScore(candidateDifficulty, difficultyCount);

            double recommendScore = 0.5 * subjectScore + 0.3 * similarityScore + 0.2 * difficultyScore;

            QuestionRecommendVO vo = createRecommendVO(candidate);
            vo.setRecommendScore(TextSimilarityUtil.roundToTwoDecimals(recommendScore));
            vo.setRecommendReason(buildRecommendReason(subjectScore, similarityScore, difficultyScore));
            results.add(vo);
        }

        if (results.isEmpty()) {
            return getDefaultRecommendations(topK);
        }

        results.sort((a, b) -> Double.compare(b.getRecommendScore(), a.getRecommendScore()));
        return results.stream().limit(topK).collect(Collectors.toList());
    }

    private List<QuestionRecommendVO> getDefaultRecommendations(int topK) {
        List<Map<String, Object>> recentQuestions = questionRecommendMapper.getRecentQuestions(topK);
        
        List<QuestionRecommendVO> results = new ArrayList<>();
        for (Map<String, Object> question : recentQuestions) {
            QuestionRecommendVO vo = createRecommendVO(question);
            vo.setRecommendScore(0.5);
            vo.setRecommendReason("暂无错题记录，默认推荐");
            results.add(vo);
        }
        
        return results;
    }

    private QuestionRecommendVO createRecommendVO(Map<String, Object> data) {
        QuestionRecommendVO vo = new QuestionRecommendVO();
        vo.setQuestionId(TextSimilarityUtil.toInteger(data.get("questionId")));
        vo.setSubjectId(TextSimilarityUtil.toInteger(data.get("subjectId")));
        vo.setDifficulty(TextSimilarityUtil.toInteger(data.get("difficulty")));
        
        String content = (String) data.get("questionContent");
        if (content != null) {
            String cleaned = TextSimilarityUtil.cleanForDisplay(content);
            vo.setQuestionContent(cleaned.isEmpty() ? "（题目内容无法解析）" : cleaned);
        } else {
            vo.setQuestionContent("");
        }
        
        return vo;
    }

    private double calculateSubjectScore(Integer subjectId, Map<Integer, Integer> subjectCount) {
        if (subjectId == null || subjectCount.isEmpty()) return 0.5;
        int count = subjectCount.getOrDefault(subjectId, 0);
        int maxCount = subjectCount.values().stream().max(Integer::compare).orElse(1);
        return Math.min(1.0, count * 1.0 / maxCount);
    }

    private double calculateSimilarityScore(String candidateText, List<String> wrongTexts) {
        if (wrongTexts.isEmpty()) return 0.0;
        double maxSimilarity = 0;
        for (String wrongText : wrongTexts) {
            if (!wrongText.isEmpty()) {
                double similarity = TextSimilarityUtil.cosineSimilarity(candidateText, wrongText);
                maxSimilarity = Math.max(maxSimilarity, similarity);
            }
        }
        return maxSimilarity;
    }

    private double calculateDifficultyScore(Integer candidateDifficulty, Map<Integer, Integer> difficultyCount) {
        if (candidateDifficulty == null || difficultyCount.isEmpty()) return 0.5;
        
        int mainDifficulty = difficultyCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(3);

        int diff = Math.abs(candidateDifficulty - mainDifficulty);
        return Math.max(0.3, 1.0 - diff * 0.3);
    }

    private String buildRecommendReason(double subjectScore, double similarityScore, double difficultyScore) {
        List<String> reasons = new ArrayList<>();
        if (subjectScore >= 0.7) reasons.add("错题科目匹配");
        if (similarityScore >= 0.6) reasons.add("题干语义相似");
        if (difficultyScore >= 0.7) reasons.add("难度适中");
        return reasons.isEmpty() ? "综合推荐" : String.join(" / ", reasons);
    }

    private Integer validateTopK(Integer topK) {
        if (topK == null || topK <= 0) {
            return DEFAULT_TOP_K;
        }
        return Math.min(topK, MAX_TOP_K);
    }
}