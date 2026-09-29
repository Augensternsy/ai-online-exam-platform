package com.mindskip.ai.service;

import com.mindskip.ai.dto.GeneratedQuestion;
import com.mindskip.ai.dto.QualityEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

/**
 * LLM-as-a-Judge 质量评估。
 * 并发受 Semaphore 限制（默认 2），评估失败（429/网络）抛出异常由调用方标记 eval_failed。
 */
@Service
public class QualityEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(QualityEvaluationService.class);

    private final LlmClient llmClient;
    private final Semaphore semaphore;

    public QualityEvaluationService(LlmClient llmClient, com.mindskip.ai.config.AiProperties aiProperties) {
        this.llmClient = llmClient;
        this.semaphore = new Semaphore(Math.max(1, aiProperties.getEvaluationConcurrency()));
    }

    /**
     * 评估单题质量，返回 1-5 分。
     * 调用失败（含 429 重试耗尽）时抛出异常，由调用方决定标记 eval_failed 而非质量不合格。
     */
    public double evaluate(String originalContent, GeneratedQuestion question) throws InterruptedException {
        semaphore.acquire();
        try {
            String prompt = buildJudgePrompt(originalContent, question);
            QualityEvaluation eval = llmClient.chatForEntity(prompt, QualityEvaluation.class);
            if (eval == null) {
                throw new RuntimeException("Quality evaluation returned null");
            }
            return eval.effectiveScore();
        } finally {
            semaphore.release();
        }
    }

    private String buildJudgePrompt(String originalContent, GeneratedQuestion q) {
        String options = q.getOptions() == null ? "" : String.join(" / ", q.getOptions());
        return "你是一位严格的试题质量评审专家。请基于以下教材背景，对给定试题进行质量评分。\n\n"
                + "【教材背景】\n" + (originalContent == null ? "" : truncate(originalContent, 2000)) + "\n\n"
                + "【试题】\n题目：" + q.getQuestion() + "\n选项：" + options
                + "\n正确答案：" + q.getCorrect_answer() + "\n解析：" + (q.getAnalysis() == null ? "" : q.getAnalysis()) + "\n\n"
                + "请从事实准确性(factuality_score)、与教材相关性(relevance_score)、难度匹配度(difficulty_alignment_score)"
                + "三个维度各打 1-5 分，并给出综合分 overall_score(1-5) 与简短反馈 feedback。";
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
