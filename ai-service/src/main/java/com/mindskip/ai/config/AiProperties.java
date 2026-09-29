package com.mindskip.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "ai.question")
public class AiProperties {

    private double qualityThreshold = 4.0;
    private int evaluationConcurrency = 2;
    private List<Long> retryBackoffMs = List.of(2000L, 5000L, 10000L);
    private int maxRetries = 3;
    private long retryAfterCapMs = 30000L;

    public double getQualityThreshold() { return qualityThreshold; }
    public void setQualityThreshold(double qualityThreshold) { this.qualityThreshold = qualityThreshold; }
    public int getEvaluationConcurrency() { return evaluationConcurrency; }
    public void setEvaluationConcurrency(int evaluationConcurrency) { this.evaluationConcurrency = evaluationConcurrency; }
    public List<Long> getRetryBackoffMs() { return retryBackoffMs; }
    public void setRetryBackoffMs(List<Long> retryBackoffMs) { this.retryBackoffMs = retryBackoffMs; }
    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public long getRetryAfterCapMs() { return retryAfterCapMs; }
    public void setRetryAfterCapMs(long retryAfterCapMs) { this.retryAfterCapMs = retryAfterCapMs; }
}
