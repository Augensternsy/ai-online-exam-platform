package com.mindskip.xzs.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindskip.xzs.configuration.property.AiQuestionConfig;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeepSeekApiClient {

    private static final Logger logger = LoggerFactory.getLogger(DeepSeekApiClient.class);

    @Autowired
    private AiQuestionConfig aiQuestionConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callChatCompletion(String prompt) throws IOException {
        return callChatCompletionWithRetry(prompt, 3);
    }

    private String callChatCompletionWithRetry(String prompt, int maxRetries) throws IOException {
        int retryCount = 0;
        IOException lastException = null;

        while (retryCount <= maxRetries) {
            try {
                return executeApiCall(prompt);
            } catch (IOException e) {
                lastException = e;
                if (e.getMessage() != null && e.getMessage().contains("429")) {
                    retryCount++;
                    if (retryCount <= maxRetries) {
                        long waitTime = (long) Math.pow(2, retryCount) * 1000;
                        logger.warn("Rate limit exceeded, retrying in {}ms (attempt {}/{})", waitTime, retryCount, maxRetries);
                        try {
                            Thread.sleep(waitTime);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Retry interrupted", ie);
                        }
                    }
                } else {
                    throw e;
                }
            }
        }

        throw lastException;
    }

    private String executeApiCall(String prompt) throws IOException {
        String apiUrl = aiQuestionConfig.getApiUrl();
        String apiKey = aiQuestionConfig.getApiKey();
        String model = aiQuestionConfig.getModel();

        logger.info("Calling DeepSeek API: {}", apiUrl);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 16000);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(120000)
                .setConnectionRequestTimeout(10000)
                .build();
        httpPost.setConfig(config);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode != 200) {
                logger.error("DeepSeek API error: status={}, body={}", statusCode, responseBody);
                throw new IOException("DeepSeek API returned error: " + statusCode + " - " + responseBody);
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            JsonNode choices = jsonNode.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    return message.get("content").asText();
                }
            }

            throw new IOException("Invalid response format from DeepSeek API");
        }
    }
}
