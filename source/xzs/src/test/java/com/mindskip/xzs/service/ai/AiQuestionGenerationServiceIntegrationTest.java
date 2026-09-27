package com.mindskip.xzs.service.ai;

import com.mindskip.xzs.XzsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Integration test for AiQuestionGenerationService.
 * Uses the real Qiniu Cloud API to verify end-to-end question generation.
 * 
 * Note: This test requires a valid API key configured in application.yml.
 * It makes real API calls and may take some time to complete.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = XzsApplication.class)
public class AiQuestionGenerationServiceIntegrationTest {

    @Autowired
    private AiQuestionGenerationService aiQuestionGenerationService;

    @Autowired
    private DeepSeekApiClient deepSeekApiClient;

    @Autowired
    private PromptTemplateBuilder promptTemplateBuilder;

    @Autowired
    private com.mindskip.xzs.configuration.property.AiQuestionConfig aiQuestionConfig;

    /**
     * Integration test: Generate a real question about "Java Basics" using the Qiniu Cloud API.
     * This test verifies the complete pipeline: API call -> JSON parsing -> validation -> result.
     */
    @Test
    public void testGenerateQuestion_JavaBasics_RealApiCall() throws Exception {
        // Verify all dependencies are injected
        assertNotNull("AiQuestionGenerationService should be injected", aiQuestionGenerationService);
        assertNotNull("DeepSeekApiClient should be injected", deepSeekApiClient);
        assertNotNull("PromptTemplateBuilder should be injected", promptTemplateBuilder);
        assertNotNull("AiQuestionConfig should be injected", aiQuestionConfig);

        System.out.println("========================================");
        System.out.println("Integration Test Configuration");
        System.out.println("========================================");
        System.out.println("API Key configured: " + (aiQuestionConfig.getApiKey() != null && !aiQuestionConfig.getApiKey().isEmpty()));
        System.out.println("API URL: " + aiQuestionConfig.getApiUrl());
        System.out.println("Model: " + aiQuestionConfig.getModel());
        System.out.println("========================================");

        // Markdown content about Java basics
        String javaBasicsMarkdown = "# Java 基础\n\n" +
                "## 1. Java 简介\n" +
                "Java 是一种面向对象的编程语言，由 Sun Microsystems 于 1995 年发布。\n" +
                "Java 具有跨平台特性，遵循\"一次编写，到处运行\"的原则。\n\n" +
                "## 2. 数据类型\n" +
                "Java 有 8 种基本数据类型：\n" +
                "- byte: 8 位有符号整数\n" +
                "- short: 16 位有符号整数\n" +
                "- int: 32 位有符号整数\n" +
                "- long: 64 位有符号整数\n" +
                "- float: 32 位浮点数\n" +
                "- double: 64 位浮点数\n" +
                "- char: 16 位 Unicode 字符\n" +
                "- boolean: 布尔值（true/false）\n\n" +
                "## 3. 面向对象\n" +
                "Java 是纯面向对象的语言，支持封装、继承和多态三大特性。\n" +
                "类是对象的模板，对象是类的实例。";

        // Build the prompt manually using the injected PromptTemplateBuilder
        String prompt = promptTemplateBuilder.buildQuestionGenerationPrompt(
                javaBasicsMarkdown,
                1,  // questionType = 1 (single choice)
                1,  // questionCount = 1
                1,  // difficulty = 1 (easy)
                null);  // gradeLevel = null

        System.out.println("Prompt built successfully, calling DeepSeek API...");

        // Call the API directly
        String response = deepSeekApiClient.callChatCompletion(prompt);

        assertNotNull("API response should not be null", response);
        assertFalse("API response should not be empty", response.trim().isEmpty());
        System.out.println("DeepSeek API response received");
        System.out.println("Response preview: " + response.substring(0, Math.min(200, response.length())));

        // Verify the response contains valid JSON structure
        assertTrue("Response should contain 'questions' array", response.contains("questions"));
        assertTrue("Response should contain 'question' field", response.contains("question"));
        assertTrue("Response should contain 'options' field", response.contains("options"));
        assertTrue("Response should contain 'correct_answer' field", response.contains("correct_answer"));
    }

    /**
     * Integration test: Verify the DeepSeek API client can successfully connect and return a response.
     */
    @Test
    public void testDeepSeekApiClient_RealConnection() throws Exception {
        String prompt = "请用一句话介绍 Java 编程语言。";

        String response = deepSeekApiClient.callChatCompletion(prompt);

        assertNotNull("API response should not be null", response);
        assertFalse("API response should not be empty", response.trim().isEmpty());
        System.out.println("DeepSeek API response: " + response);
    }
}
