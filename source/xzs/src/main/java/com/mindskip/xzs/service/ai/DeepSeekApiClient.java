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

    /** 限流退避截止时间戳，用于前端进度展示（volatile 保证跨线程可见） */
    private volatile long rateLimitBackoffUntil = 0L;

    /** 当前是否处于 429 退避等待中 */
    public boolean isRateLimited() {
        return System.currentTimeMillis() < rateLimitBackoffUntil;
    }

    /** 429 专用异常，携带服务端 Retry-After 提示（毫秒，0 表示未提供） */
    public static class RateLimitException extends IOException {
        private final long retryAfterMs;

        public RateLimitException(String message, long retryAfterMs) {
            super(message);
            this.retryAfterMs = retryAfterMs;
        }

        public long getRetryAfterMs() {
            return retryAfterMs;
        }
    }

    public String callChatCompletion(String prompt) throws IOException {
        return callChatCompletionWithRetry(prompt, 3);
    }

    private String callChatCompletionWithRetry(String prompt, int maxRetries) throws IOException {
        // 429 指数退避：2s -> 5s -> 10s；若响应携带 Retry-After 则优先遵循（上限 30s）
        long[] backoffMs = {2000L, 5000L, 10000L};
        IOException lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return executeApiCall(prompt);
            } catch (RateLimitException e) {
                lastException = e;
                if (attempt >= maxRetries) {
                    break;
                }
                long wait = e.getRetryAfterMs() > 0
                        ? Math.min(e.getRetryAfterMs(), 30000L)
                        : backoffMs[Math.min(attempt, backoffMs.length - 1)];
                rateLimitBackoffUntil = System.currentTimeMillis() + wait;
                logger.warn("Rate limit (429), retrying in {}ms (retry {}/{})", wait, attempt + 1, maxRetries);
                sleepQuietly(wait);
            } catch (IOException e) {
                throw e;
            }
        }

        throw lastException;
    }

    private void sleepQuietly(long millis) throws IOException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IOException("Retry interrupted", ie);
        }
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

            if (statusCode == 429) {
                long retryAfterMs = parseRetryAfterMs(response.getFirstHeader("Retry-After"));
                logger.error("DeepSeek API rate limited: 429, retryAfter={}, body={}", retryAfterMs, responseBody);
                throw new RateLimitException("DeepSeek API rate limited: 429 - " + responseBody, retryAfterMs);
            }

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

    /** 解析 Retry-After 头（秒或 HTTP 日期），无法解析返回 0 */
    private long parseRetryAfterMs(org.apache.http.Header header) {
        if (header == null) {
            return 0L;
        }
        try {
            return Long.parseLong(header.getValue().trim()) * 1000L;
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }
}
