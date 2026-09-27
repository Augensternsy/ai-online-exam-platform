package com.mindskip.xzs.configuration.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai.question")
public class AiQuestionConfig {

    private String apiKey;
    private String apiUrl;
    private String model;
    private String judgeModel;
    private int maxRetries = 3;
    private long retryDelayMs = 1000;
    private double qualityThreshold = 4.0;
    private int defaultQuestionCount = 10;
    private int defaultQuestionType = 1;
    private int defaultDifficulty = 2;
    
    private String provider = "deepseek";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getJudgeModel() {
        return judgeModel;
    }

    public void setJudgeModel(String judgeModel) {
        this.judgeModel = judgeModel;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    public void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    public double getQualityThreshold() {
        return qualityThreshold;
    }

    public void setQualityThreshold(double qualityThreshold) {
        this.qualityThreshold = qualityThreshold;
    }

    public int getDefaultQuestionCount() {
        return defaultQuestionCount;
    }

    public void setDefaultQuestionCount(int defaultQuestionCount) {
        this.defaultQuestionCount = defaultQuestionCount;
    }

    public int getDefaultQuestionType() {
        return defaultQuestionType;
    }

    public void setDefaultQuestionType(int defaultQuestionType) {
        this.defaultQuestionType = defaultQuestionType;
    }

    public int getDefaultDifficulty() {
        return defaultDifficulty;
    }

    public void setDefaultDifficulty(int defaultDifficulty) {
        this.defaultDifficulty = defaultDifficulty;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
