package com.mindskip.xzs.service.ai;

import com.mindskip.xzs.configuration.property.AiQuestionConfig;
import com.mindskip.xzs.repository.QuestionMapper;
import com.mindskip.xzs.repository.TextContentMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for AiQuestionGenerationService.
 * Tests the JSON parsing and validation logic by mocking the external DeepSeek API.
 * No real network calls are made - all API responses are simulated.
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class AiQuestionGenerationServiceUnitTest {

    @Mock
    private AiQuestionConfig aiQuestionConfig;

    @Mock
    private DeepSeekApiClient deepSeekApiClient;

    @Mock
    private PromptTemplateBuilder promptTemplateBuilder;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private TextContentMapper textContentMapper;

    @InjectMocks
    private AiQuestionGenerationService aiQuestionGenerationService;

    @Before
    public void setUp() {
        when(aiQuestionConfig.getMaxRetries()).thenReturn(3);
        when(aiQuestionConfig.getRetryDelayMs()).thenReturn(100L);
        when(aiQuestionConfig.getQualityThreshold()).thenReturn(4.0);
        when(aiQuestionConfig.getDefaultQuestionType()).thenReturn(1);
        when(aiQuestionConfig.getDefaultQuestionCount()).thenReturn(10);
        when(aiQuestionConfig.getDefaultDifficulty()).thenReturn(2);
        when(aiQuestionConfig.getJudgeModel()).thenReturn("deepseek-v3");
        when(promptTemplateBuilder.buildQuestionGenerationPrompt(anyString(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn("test prompt");
        when(promptTemplateBuilder.buildJudgePrompt(anyString(), anyString(), any(), anyString(), anyString()))
                .thenReturn("judge prompt");
    }

    /**
     * Test that a valid JSON response from DeepSeek API is correctly parsed and validated.
     * This verifies the core parsing logic without making any real API calls.
     */
    @Test
    public void testParseAndValidateQuestions_ValidResponse() throws Exception {
        String mockApiResponse = "{\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"Java中哪个关键字用于定义类？\",\n" +
                "      \"options\": [\"A. class\", \"B. interface\", \"C. enum\", \"D. struct\"],\n" +
                "      \"correct_answer\": \"A\",\n" +
                "      \"analysis\": \"class关键字用于定义类\",\n" +
                "      \"knowledge_point\": \"Java基础语法\",\n" +
                "      \"difficulty\": 1\n" +
                "    },\n" +
                "    {\n" +
                "      \"question\": \"以下哪个是Java的基本数据类型？\",\n" +
                "      \"options\": [\"A. String\", \"B. int\", \"C. Object\", \"D. ArrayList\"],\n" +
                "      \"correct_answer\": \"B\",\n" +
                "      \"analysis\": \"int是Java的8种基本数据类型之一\",\n" +
                "      \"knowledge_point\": \"Java数据类型\",\n" +
                "      \"difficulty\": 1\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(deepSeekApiClient.callChatCompletion(anyString())).thenReturn(mockApiResponse);

        Method method = AiQuestionGenerationService.class.getDeclaredMethod(
                "generateQuestionsWithRetry", String.class, Integer.class, Integer.class, Integer.class, Integer.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) method.invoke(
                aiQuestionGenerationService, "test markdown content", 1, 2, 1, null);

        assertNotNull(questions);
        assertEquals(2, questions.size());

        Map<String, Object> q1 = questions.get(0);
        assertEquals("Java中哪个关键字用于定义类？", q1.get("question"));
        assertEquals("A", q1.get("correct_answer"));
        assertEquals("class关键字用于定义类", q1.get("analysis"));
        assertEquals("Java基础语法", q1.get("knowledge_point"));
        assertEquals(1, q1.get("difficulty"));

        @SuppressWarnings("unchecked")
        List<String> options1 = (List<String>) q1.get("options");
        assertEquals(4, options1.size());
        assertTrue(options1.get(0).startsWith("A."));

        Map<String, Object> q2 = questions.get(1);
        assertEquals("以下哪个是Java的基本数据类型？", q2.get("question"));
        assertEquals("B", q2.get("correct_answer"));
    }

    /**
     * Test that invalid JSON response triggers retry mechanism.
     * First two attempts return invalid JSON, third attempt succeeds.
     */
    @Test
    public void testParseAndValidateQuestions_InvalidJson_Retries() throws Exception {
        String invalidResponse = "This is not valid JSON";
        String validResponse = "{\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"测试题目\",\n" +
                "      \"options\": [\"A. 选项1\", \"B. 选项2\", \"C. 选项3\", \"D. 选项4\"],\n" +
                "      \"correct_answer\": \"A\",\n" +
                "      \"analysis\": \"解析\",\n" +
                "      \"knowledge_point\": \"知识点\",\n" +
                "      \"difficulty\": 2\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(deepSeekApiClient.callChatCompletion(anyString()))
                .thenReturn(invalidResponse)
                .thenReturn(invalidResponse)
                .thenReturn(validResponse);

        Method method = AiQuestionGenerationService.class.getDeclaredMethod(
                "generateQuestionsWithRetry", String.class, Integer.class, Integer.class, Integer.class, Integer.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) method.invoke(
                aiQuestionGenerationService, "test markdown", 1, 1, 1, null);

        assertNotNull(questions);
        assertEquals(1, questions.size());
        assertEquals("测试题目", questions.get(0).get("question"));

        verify(deepSeekApiClient, times(3)).callChatCompletion(anyString());
    }

    /**
     * Test that questions with correct_answer not matching any option prefix are filtered out.
     */
    @Test
    public void testParseAndValidateQuestions_CorrectAnswerNotInOptions_Filtered() throws Exception {
        String mockApiResponse = "{\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"有效题目\",\n" +
                "      \"options\": [\"A. 选项1\", \"B. 选项2\", \"C. 选项3\", \"D. 选项4\"],\n" +
                "      \"correct_answer\": \"A\",\n" +
                "      \"analysis\": \"解析\",\n" +
                "      \"knowledge_point\": \"知识点\",\n" +
                "      \"difficulty\": 2\n" +
                "    },\n" +
                "    {\n" +
                "      \"question\": \"无效题目-选项前缀不匹配\",\n" +
                "      \"options\": [\"1. 选项1\", \"2. 选项2\", \"3. 选项3\", \"4. 选项4\"],\n" +
                "      \"correct_answer\": \"A\",\n" +
                "      \"analysis\": \"解析\",\n" +
                "      \"knowledge_point\": \"知识点\",\n" +
                "      \"difficulty\": 2\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(deepSeekApiClient.callChatCompletion(anyString())).thenReturn(mockApiResponse);

        Method method = AiQuestionGenerationService.class.getDeclaredMethod(
                "generateQuestionsWithRetry", String.class, Integer.class, Integer.class, Integer.class, Integer.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) method.invoke(
                aiQuestionGenerationService, "test markdown", 1, 2, 1, null);

        assertNotNull(questions);
        assertEquals(1, questions.size());
        assertEquals("有效题目", questions.get(0).get("question"));
    }

    /**
     * Test that missing optional fields get default values.
     * Note: analysis is required by JSON Schema, so it must be present.
     * Only knowledge_point and difficulty are truly optional.
     */
    @Test
    public void testParseAndValidateQuestions_MissingOptionalFields_DefaultValues() throws Exception {
        String mockApiResponse = "{\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"只有必填字段的题目\",\n" +
                "      \"options\": [\"A. 选项1\", \"B. 选项2\", \"C. 选项3\", \"D. 选项4\"],\n" +
                "      \"correct_answer\": \"C\",\n" +
                "      \"analysis\": \"解析内容\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(deepSeekApiClient.callChatCompletion(anyString())).thenReturn(mockApiResponse);

        Method method = AiQuestionGenerationService.class.getDeclaredMethod(
                "generateQuestionsWithRetry", String.class, Integer.class, Integer.class, Integer.class, Integer.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) method.invoke(
                aiQuestionGenerationService, "test markdown", 1, 1, 1, null);

        assertNotNull(questions);
        assertEquals(1, questions.size());

        Map<String, Object> q = questions.get(0);
        assertEquals("解析内容", q.get("analysis"));
        assertEquals("", q.get("knowledge_point"));
        assertEquals(2, q.get("difficulty"));
    }

    /**
     * Test that JSON Schema validation rejects responses missing required fields.
     */
    @Test(expected = Exception.class)
    public void testParseAndValidateQuestions_MissingRequiredFields_ThrowsException() throws Exception {
        String invalidResponse = "{\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"缺少选项的题目\",\n" +
                "      \"correct_answer\": \"A\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(deepSeekApiClient.callChatCompletion(anyString())).thenReturn(invalidResponse);

        Method method = AiQuestionGenerationService.class.getDeclaredMethod(
                "generateQuestionsWithRetry", String.class, Integer.class, Integer.class, Integer.class, Integer.class);
        method.setAccessible(true);

        method.invoke(aiQuestionGenerationService, "test markdown", 1, 1, 1, null);
    }

    /**
     * Test that correct_answer pattern validation works (must be A-D).
     */
    @Test(expected = Exception.class)
    public void testParseAndValidateQuestions_InvalidCorrectAnswerPattern_ThrowsException() throws Exception {
        String invalidResponse = "{\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"question\": \"正确答案格式错误\",\n" +
                "      \"options\": [\"A. 选项1\", \"B. 选项2\", \"C. 选项3\", \"D. 选项4\"],\n" +
                "      \"correct_answer\": \"AB\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        when(deepSeekApiClient.callChatCompletion(anyString())).thenReturn(invalidResponse);

        Method method = AiQuestionGenerationService.class.getDeclaredMethod(
                "generateQuestionsWithRetry", String.class, Integer.class, Integer.class, Integer.class, Integer.class);
        method.setAccessible(true);

        method.invoke(aiQuestionGenerationService, "test markdown", 1, 1, 1, null);
    }
}
