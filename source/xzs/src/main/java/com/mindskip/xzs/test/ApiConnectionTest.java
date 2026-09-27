package com.mindskip.xzs.test;

import com.mindskip.xzs.configuration.property.AiQuestionConfig;
import com.mindskip.xzs.service.ai.DeepSeekApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApiConnectionTest implements CommandLineRunner {

    @Autowired
    private AiQuestionConfig aiQuestionConfig;

    @Autowired
    private DeepSeekApiClient deepSeekApiClient;

    @Override
    public void run(String... args) throws Exception {
        String apiKey = aiQuestionConfig.getApiKey();
        boolean hasKey = apiKey != null && !apiKey.trim().isEmpty();
        System.out.println("========================================");
        System.out.println("七牛云 AI 推理服务连接测试");
        System.out.println("========================================");
        System.out.println("API URL: " + aiQuestionConfig.getApiUrl());
        System.out.println("Model: " + aiQuestionConfig.getModel());
        System.out.println("API Key: " + (hasKey ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "未配置（将使用内置兜底题库）"));
        System.out.println("========================================");

        if (!hasKey) {
            System.out.println("⚠️  未配置 AI API Key，跳过连接测试。Exam Agent 将自动回退到内置兜底题库。");
            return;
        }

        try {
            String testPrompt = "请用一句话介绍你自己。";
            System.out.println("\n发送测试请求...");
            
            String response = deepSeekApiClient.callChatCompletion(testPrompt);
            
            System.out.println("\n✅ API 连接成功！");
            System.out.println("响应内容: " + response);
            System.out.println("\n========================================");
            System.out.println("测试完成 - 七牛云 AI 服务可用");
            System.out.println("========================================");
            
        } catch (Exception e) {
            System.err.println("\n❌ API 连接失败！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("\n请检查：");
            System.err.println("1. API Key 是否正确");
            System.err.println("2. 网络连接是否正常");
            System.err.println("3. 七牛云账户是否有可用额度");
            System.err.println("========================================");
        }
    }
}
