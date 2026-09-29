package com.mindskip.ai.agent;

import com.mindskip.ai.config.AiProperties;
import com.mindskip.ai.dto.GeneratedQuestion;
import com.mindskip.ai.service.QualityEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/** 步骤四：校验评估 —— 并发受限的 LLM-as-a-Judge，区分质量淘汰与评估失败。 */
@Component
public class QualityEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(QualityEvaluator.class);

    private final QualityEvaluationService evaluationService;
    private final AiProperties aiProperties;
    private final ExecutorService executor;

    public QualityEvaluator(QualityEvaluationService evaluationService, AiProperties aiProperties) {
        this.evaluationService = evaluationService;
        this.aiProperties = aiProperties;
        this.executor = Executors.newFixedThreadPool(Math.max(1, aiProperties.getEvaluationConcurrency()));
    }

    public EvaluationResult evaluate(String originalContent, List<GeneratedQuestion> questions) {
        long start = System.currentTimeMillis();
        List<CompletableFuture<GeneratedQuestion>> futures = questions.stream()
                .map(q -> CompletableFuture.supplyAsync(() -> {
                    try {
                        double score = evaluationService.evaluate(originalContent, q);
                        q.setQualityScore(score);
                        q.setStatus(score >= aiProperties.getQualityThreshold() ? "qualified" : "rejected");
                    } catch (Exception e) {
                        // 评估调用失败（429 重试耗尽/网络）：保留题目并标记 eval_failed，不计入 rejected
                        logger.error("Quality evaluation failed, marking eval_failed: {}", e.getMessage());
                        q.setStatus("eval_failed");
                    }
                    return q;
                }, executor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        EvaluationResult result = new EvaluationResult();
        for (CompletableFuture<GeneratedQuestion> f : futures) {
            GeneratedQuestion q = f.join();
            if ("qualified".equals(q.getStatus())) {
                result.qualified.add(q);
            } else if ("eval_failed".equals(q.getStatus())) {
                result.evaluationFailed.add(q);
            } else {
                result.rejected.add(q);
            }
        }
        logger.info("Evaluation done: qualified={}, rejected={}, evalFailed={}, time={}ms",
                result.qualified.size(), result.rejected.size(), result.evaluationFailed.size(),
                System.currentTimeMillis() - start);
        return result;
    }

    public static class EvaluationResult {
        public final List<GeneratedQuestion> qualified = new ArrayList<>();
        public final List<GeneratedQuestion> rejected = new ArrayList<>();
        public final List<GeneratedQuestion> evaluationFailed = new ArrayList<>();
    }
}
