package com.mindskip.ai.service;

import com.mindskip.ai.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 封装对 Spring AI ChatClient 的调用。
 * 所有模型调用统一走这里，包含 429 限流指数退避与 Retry-After 处理。
 */
@Service
public class LlmClient {

    private static final Logger logger = LoggerFactory.getLogger(LlmClient.class);

    private final ChatClient chatClient;
    private final AiProperties aiProperties;

    /** 限流退避截止时间戳，供进度查询展示（volatile 保证跨线程可见） */
    private volatile long rateLimitBackoffUntil = 0L;

    public LlmClient(ChatClient chatClient, AiProperties aiProperties) {
        this.chatClient = chatClient;
        this.aiProperties = aiProperties;
    }

    public boolean isRateLimited() {
        return System.currentTimeMillis() < rateLimitBackoffUntil;
    }

    /** 纯文本对话 */
    public String chat(String prompt) {
        return callWithRetry(() -> chatClient.prompt().user(prompt).call().content());
    }

    /** 结构化输出（Spring AI BeanOutputConverter） */
    public <T> T chatForEntity(String prompt, Class<T> entityClass) {
        BeanOutputConverter<T> converter = new BeanOutputConverter<>(entityClass);
        String fullPrompt = prompt + "\n\n" + converter.getFormat();
        String raw = callWithRetry(() -> chatClient.prompt().user(fullPrompt).call().content());
        return converter.convert(raw);
    }

    @FunctionalInterface
    private interface AiCall<T> {
        T execute();
    }

    private <T> T callWithRetry(AiCall<T> call) {
        List<Long> backoff = aiProperties.getRetryBackoffMs();
        int maxRetries = aiProperties.getMaxRetries();
        RuntimeException last = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return call.execute();
            } catch (Exception e) {
                if (!isRateLimit(e)) {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                }
                last = e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                if (attempt >= maxRetries) {
                    break;
                }
                long retryAfter = extractRetryAfterMs(e);
                long wait = retryAfter > 0
                        ? Math.min(retryAfter, aiProperties.getRetryAfterCapMs())
                        : backoff.get(Math.min(attempt, backoff.size() - 1));
                rateLimitBackoffUntil = System.currentTimeMillis() + wait;
                logger.warn("Rate limit (429), retrying in {}ms (retry {}/{})", wait, attempt + 1, maxRetries);
                sleepQuietly(wait);
            }
        }
        throw last;
    }

    /** 判断异常链中是否包含 429 / rate limit 信号 */
    private boolean isRateLimit(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null && (msg.contains("429") || msg.toLowerCase().contains("rate limit")
                    || msg.toLowerCase().contains("rate_limit") || msg.toLowerCase().contains("too many requests"))) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    /** 从异常消息中提取 Retry-After（毫秒），无法解析返回 0 */
    private long extractRetryAfterMs(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null) {
                // 常见形式："Retry-After: 2" 或 "retry after 2 seconds"
                java.util.regex.Matcher m = java.util.regex.Pattern
                        .compile("[Rr]etry[- ][Aa]fter[: ]+([0-9]+)").matcher(msg);
                if (m.find()) {
                    try {
                        return Long.parseLong(m.group(1)) * 1000L;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            cur = cur.getCause();
        }
        return 0L;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry interrupted", ie);
        }
    }
}
