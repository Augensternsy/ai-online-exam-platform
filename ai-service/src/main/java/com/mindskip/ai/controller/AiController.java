package com.mindskip.ai.controller;

import com.mindskip.ai.dto.QuestionGenerateRequest;
import com.mindskip.ai.service.LlmClient;
import com.mindskip.ai.service.QuestionGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final Logger logger = LoggerFactory.getLogger(AiController.class);

    private final LlmClient llmClient;
    private final QuestionGenerationService questionGenerationService;

    public AiController(LlmClient llmClient, QuestionGenerationService questionGenerationService) {
        this.llmClient = llmClient;
        this.questionGenerationService = questionGenerationService;
    }

    /** 基础对话：POST /api/ai/chat { "prompt": "..." } -> { "content": "..." } */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "prompt 不能为空"));
        }
        try {
            String content = llmClient.chat(prompt);
            Map<String, Object> resp = new HashMap<>();
            resp.put("content", content);
            resp.put("rateLimited", llmClient.isRateLimited());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            logger.error("Chat failed", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /** AI 出题：POST /api/ai/questions/generate */
    @PostMapping("/questions/generate")
    public ResponseEntity<Map<String, Object>> generateQuestions(@RequestBody QuestionGenerateRequest request) {
        try {
            Map<String, Object> result = questionGenerationService.generate(request);
            result.put("rateLimited", llmClient.isRateLimited());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Question generation failed", e);
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    /** 限流状态查询（供主系统进度页轮询） */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("rateLimited", llmClient.isRateLimited());
    }
}
