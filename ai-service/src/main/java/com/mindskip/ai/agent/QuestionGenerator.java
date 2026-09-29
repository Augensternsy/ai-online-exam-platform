package com.mindskip.ai.agent;

import com.mindskip.ai.dto.GeneratedQuestion;
import com.mindskip.ai.dto.GeneratedQuestionsResponse;
import com.mindskip.ai.service.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** 步骤三：执行生成 —— 调用 Spring AI 生成结构化题目，并做本地合法性过滤。 */
@Component
public class QuestionGenerator {

    private static final Logger logger = LoggerFactory.getLogger(QuestionGenerator.class);

    private final LlmClient llmClient;

    public QuestionGenerator(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public List<GeneratedQuestion> generate(String prompt) {
        GeneratedQuestionsResponse response = llmClient.chatForEntity(prompt, GeneratedQuestionsResponse.class);
        if (response == null || response.getQuestions() == null) {
            logger.warn("LLM returned null questions");
            return new ArrayList<>();
        }
        List<GeneratedQuestion> valid = response.getQuestions().stream()
                .filter(this::isValid)
                .collect(Collectors.toList());
        logger.info("Generated {} valid questions (raw {})", valid.size(), response.getQuestions().size());
        return valid;
    }

    private boolean isValid(GeneratedQuestion q) {
        if (q == null || q.getQuestion() == null || q.getQuestion().isBlank()) return false;
        if (q.getOptions() == null || q.getOptions().size() < 2) return false;
        if (q.getCorrect_answer() == null || q.getCorrect_answer().isBlank()) return false;
        return true;
    }
}
