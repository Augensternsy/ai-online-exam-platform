package com.mindskip.xzs.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 调用独立 ai-service 微服务的 HTTP 客户端。
 * 仅在 ai.provider=spring-ai 时启用。
 */
@Component
public class SpringAiServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(SpringAiServiceClient.class);

    @Value("${ai.service.base-url:http://localhost:8081}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 调用 ai-service 生成题目（FAST 或 STANDARD 模式） */
    public Map<String, Object> generateQuestions(String subject, String difficulty, Integer questionCount,
            String questionTypes, String mode, String content) throws IOException {
        String url = baseUrl + "/api/ai/questions/generate";

        Map<String, Object> requestBody = new java.util.HashMap<>();
        requestBody.put("subject", subject);
        requestBody.put("difficulty", difficulty);
        requestBody.put("questionCount", questionCount);
        if (questionTypes != null && !questionTypes.isEmpty()) {
            requestBody.put("questionTypes", java.util.Arrays.asList(questionTypes.split(",")));
        }
        requestBody.put("mode", mode);
        if (content != null && !content.isEmpty()) {
            requestBody.put("content", content);
        }

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(300000)
                .setConnectionRequestTimeout(10000)
                .build();
        httpPost.setConfig(config);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(httpPost)) {

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode != 200) {
                logger.error("ai-service error: status={}, body={}", statusCode, responseBody);
                throw new IOException("ai-service returned error: " + statusCode + " - " + responseBody);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);
            return result;
        }
    }

    /** 查询 ai-service 限流状态 */
    public boolean isRateLimited() {
        try {
            String url = baseUrl + "/api/ai/status";
            org.apache.http.client.methods.HttpGet httpGet = new org.apache.http.client.methods.HttpGet(url);
            httpGet.setConfig(RequestConfig.custom().setConnectTimeout(5000).setSocketTimeout(5000).build());

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                    CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> status = objectMapper.readValue(body, Map.class);
                    return Boolean.TRUE.equals(status.get("rateLimited"));
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to query ai-service status: {}", e.getMessage());
        }
        return false;
    }
}
