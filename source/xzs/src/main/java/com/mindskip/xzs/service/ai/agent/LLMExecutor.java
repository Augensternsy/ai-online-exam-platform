package com.mindskip.xzs.service.ai.agent;

import com.mindskip.xzs.service.ai.DeepSeekApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * LLM 执行模块：封装对 DeepSeek 的调用，统一异常处理与超时。
 * 当前复用已有的 DeepSeekApiClient（Apache HttpClient 实现），
 * 后续可平滑替换为 Spring AI ChatClient 而不影响上层 Agent。
 */
@Component
public class LLMExecutor {

    private static final Logger logger = LoggerFactory.getLogger(LLMExecutor.class);

    @Autowired
    private DeepSeekApiClient deepSeekApiClient;

    /**
     * 调用 LLM，返回原始文本内容。
     * 异常由调用方（ExamAgent）决定是否重试。
     */
    public String call(String prompt) throws Exception {
        long start = System.currentTimeMillis();
        try {
            String content = deepSeekApiClient.callChatCompletion(prompt);
            logger.info("LLM call completed in {}ms, response length={}", System.currentTimeMillis() - start,
                    content == null ? 0 : content.length());
            return content;
        } catch (Exception e) {
            logger.error("LLM call failed after {}ms: {}", System.currentTimeMillis() - start, e.getMessage());
            throw e;
        }
    }
}
