package com.mindskip.xzs.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.mindskip.xzs.configuration.property.AiQuestionConfig;
import com.mindskip.xzs.domain.Question;
import com.mindskip.xzs.domain.TextContent;
import com.mindskip.xzs.domain.enums.QuestionTypeEnum;
import com.mindskip.xzs.domain.question.QuestionItemObject;
import com.mindskip.xzs.domain.question.QuestionObject;
import com.mindskip.xzs.repository.QuestionMapper;
import com.mindskip.xzs.repository.TextContentMapper;
import com.mindskip.xzs.utility.PdfToMarkdownUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class AiQuestionGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(AiQuestionGenerationService.class);

    @Autowired
    private AiQuestionConfig aiQuestionConfig;

    @Autowired
    private DeepSeekApiClient deepSeekApiClient;

    @Autowired(required = false)
    private SpringAiServiceClient springAiServiceClient;

    @org.springframework.beans.factory.annotation.Value("${ai.provider:legacy}")
    private String aiProvider;

    @Autowired
    private PromptTemplateBuilder promptTemplateBuilder;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private TextContentMapper textContentMapper;

    @Autowired
    private ToolCallService toolCallService;

    @Autowired
    private QuestionDuplicateCheckService questionDuplicateCheckService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonSchema questionSchema;
    // 质量评估并发数限制为 2，避免触发 API RPM 限流
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    /** 生成任务进度表：taskId -> {stage, evaluated, total, message, updatedAt} */
    private final ConcurrentHashMap<String, Map<String, Object>> progressMap = new ConcurrentHashMap<>();

    /** 查询任务进度（附加实时限流状态），任务不存在返回 null */
    public Map<String, Object> getProgress(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            return null;
        }
        Map<String, Object> progress = progressMap.get(taskId);
        if (progress == null) {
            return null;
        }
        Map<String, Object> copy = new HashMap<>(progress);
        copy.put("rateLimited", "spring-ai".equalsIgnoreCase(aiProvider) && springAiServiceClient != null
                ? springAiServiceClient.isRateLimited()
                : deepSeekApiClient.isRateLimited());
        return copy;
    }

    private void updateProgress(String taskId, String stage, int evaluated, int total, String message) {
        if (taskId == null || taskId.isEmpty()) {
            return;
        }
        Map<String, Object> progress = new HashMap<>();
        progress.put("stage", stage);
        progress.put("evaluated", evaluated);
        progress.put("total", total);
        progress.put("message", message);
        progress.put("updatedAt", System.currentTimeMillis());
        progressMap.put(taskId, progress);
    }

    public AiQuestionGenerationService() throws ProcessingException, IOException {
        String schemaJson = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"required\": [\"questions\"],\n" +
                "  \"properties\": {\n" +
                "    \"questions\": {\n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"required\": [\"question\", \"options\", \"correct_answer\"],\n" +
                "        \"properties\": {\n" +
                "          \"question\": {\"type\": \"string\"},\n" +
                "          \"options\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": {\"type\": \"string\"},\n" +
                "            \"minItems\": 2\n" +
                "          },\n" +
                "          \"correct_answer\": {\"type\": \"string\"},\n" +
                "          \"analysis\": {\"type\": \"string\"},\n" +
                "          \"knowledge_point\": {\"type\": \"string\"},\n" +
                "          \"difficulty\": {\"type\": \"integer\", \"minimum\": 1, \"maximum\": 3}\n" +
                "        }\n" +
                "      },\n" +
                "      \"minItems\": 1\n" +
                "    }\n" +
                "  }\n" +
                "}";
        JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
        JsonNode schemaNode = JsonLoader.fromString(schemaJson);
        this.questionSchema = factory.getJsonSchema(schemaNode);
    }

    public Map<String, Object> generateQuestionsFromPdf(MultipartFile pdfFile, Integer subjectId, Integer gradeLevel,
                                                         Integer questionType, Integer questionCount, Integer difficulty,
                                                         Boolean enableQualityCheck, String taskId) throws Exception {
        logger.info("Starting AI question generation from PDF, qualityCheck={}, provider={}", enableQualityCheck, aiProvider);

        // 使用 Spring AI 微服务（ai.provider=spring-ai）
        if ("spring-ai".equalsIgnoreCase(aiProvider) && springAiServiceClient != null) {
            return generateQuestionsViaSpringAi(pdfFile, subjectId, gradeLevel, questionType, questionCount,
                    difficulty, enableQualityCheck, taskId);
        }

        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            updateProgress(taskId, "parsing", 0, 0, "正在解析 PDF 教材内容...");
            String markdownContent = convertPdfToMarkdown(pdfFile);
            result.put("markdownContent", markdownContent);

            updateProgress(taskId, "generating", 0, 0, "正在生成题目...");
            List<Map<String, Object>> generatedQuestions = generateQuestionsWithRetry(
                    markdownContent, questionType, questionCount, difficulty, gradeLevel, subjectId);

            List<Map<String, Object>> qualifiedQuestions = new ArrayList<>();
            List<Map<String, Object>> rejectedQuestions = new ArrayList<>();
            List<Map<String, Object>> evaluationFailedQuestions = new ArrayList<>();

            if (enableQualityCheck != null && enableQualityCheck) {
                int total = generatedQuestions.size();
                AtomicInteger evaluatedCount = new AtomicInteger(0);
                logger.info("Starting quality evaluation for {} questions (concurrency=2)", total);
                long evalStartTime = System.currentTimeMillis();
                updateProgress(taskId, "evaluating", 0, total, "正在进行质量评估（0/" + total + "）...");

                final String fTaskId = taskId;
                List<CompletableFuture<Map<String, Object>>> futures = generatedQuestions.stream()
                        .map(question -> CompletableFuture.supplyAsync(() -> {
                            try {
                                double qualityScore = evaluateQuestionQuality(markdownContent, question);
                                question.put("qualityScore", qualityScore);
                                if (qualityScore >= aiQuestionConfig.getQualityThreshold()) {
                                    question.put("status", "qualified");
                                } else {
                                    question.put("status", "rejected");
                                }
                            } catch (Exception e) {
                                // 评估调用失败（如 429 重试耗尽）：保留题目并标记待评估，不计入 rejected
                                logger.error("Quality evaluation failed, marking as eval_failed: {}", e.getMessage());
                                question.put("status", "eval_failed");
                            } finally {
                                int evaluated = evaluatedCount.incrementAndGet();
                                updateProgress(fTaskId, "evaluating", evaluated, total,
                                        "正在进行质量评估（" + evaluated + "/" + total + "）...");
                            }
                            return question;
                        }, executorService))
                        .collect(Collectors.toList());

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                for (CompletableFuture<Map<String, Object>> future : futures) {
                    Map<String, Object> question = future.get();
                    if ("qualified".equals(question.get("status"))) {
                        qualifiedQuestions.add(question);
                    } else if ("eval_failed".equals(question.get("status"))) {
                        evaluationFailedQuestions.add(question);
                    } else {
                        rejectedQuestions.add(question);
                    }
                }

                long evalEndTime = System.currentTimeMillis();
                logger.info("Quality evaluation completed in {}ms (parallel processing)", evalEndTime - evalStartTime);
            } else {
                logger.info("Quality check disabled, marking all {} questions as qualified", generatedQuestions.size());
                for (Map<String, Object> question : generatedQuestions) {
                    question.put("qualityScore", 5.0);
                    question.put("status", "qualified");
                    qualifiedQuestions.add(question);
                }
            }

            result.put("qualifiedQuestions", qualifiedQuestions);
            result.put("rejectedQuestions", rejectedQuestions);
            result.put("evaluationFailedQuestions", evaluationFailedQuestions);
            result.put("totalCount", generatedQuestions.size());
            result.put("qualifiedCount", qualifiedQuestions.size());
            result.put("rejectedCount", rejectedQuestions.size());
            result.put("evaluationFailedCount", evaluationFailedQuestions.size());
            result.put("processingTime", System.currentTimeMillis() - startTime);

            updateProgress(taskId, "done", evaluationFailedQuestions.size() + qualifiedQuestions.size() + rejectedQuestions.size(),
                    generatedQuestions.size(), "生成完成");

            logger.info("Question generation completed: total={}, qualified={}, rejected={}, evaluationFailed={}, time={}ms",
                    generatedQuestions.size(), qualifiedQuestions.size(), rejectedQuestions.size(),
                    evaluationFailedQuestions.size(), System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            logger.error("Question generation failed", e);
            updateProgress(taskId, "failed", 0, 0, "生成失败：" + e.getMessage());
            result.put("error", e.getMessage());
            throw e;
        }

        return result;
    }

    @Transactional
    public List<Integer> saveQuestionsToDatabase(List<Map<String, Object>> questions, Integer subjectId,
                                                   Integer gradeLevel, Integer questionType, Integer createUser) {
        List<Integer> savedQuestionIds = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (Map<String, Object> questionData : questions) {
            try {
                Integer currentQuestionType = questionType != null ? questionType : 
                        (Integer) questionData.getOrDefault("questionType", QuestionTypeEnum.SingleChoice.getCode());
                
                Question question = new Question();
                question.setQuestionType(currentQuestionType);
                question.setSubjectId(subjectId);
                question.setGradeLevel(gradeLevel);
                question.setDifficult((Integer) questionData.getOrDefault("difficulty", 2));
                // 默认每题 10 分（内部单位 100），判分以题库分为准，避免 1000（=100分/题）导致得分超过试卷满分
                question.setScore(100);
                question.setCreateUser(createUser);
                question.setStatus(1);
                question.setDeleted(false);
                question.setCreateTime(new Date());

                QuestionObject questionObject = new QuestionObject();
                questionObject.setTitleContent((String) questionData.get("question"));

                String correctAnswer = (String) questionData.get("correct_answer");

                if (currentQuestionType == QuestionTypeEnum.SingleChoice.getCode() ||
                    currentQuestionType == QuestionTypeEnum.MultipleChoice.getCode() ||
                    currentQuestionType == QuestionTypeEnum.TrueFalse.getCode()) {
                    
                    List<String> options = (List<String>) questionData.get("options");
                    if (options != null && !options.isEmpty()) {
                        List<QuestionItemObject> itemObjects = new ArrayList<>();
                        for (int i = 0; i < options.size(); i++) {
                            QuestionItemObject item = new QuestionItemObject();
                            item.setPrefix(String.valueOf((char) ('A' + i)));
                            item.setContent(options.get(i));
                            item.setScore(100);
                            itemObjects.add(item);
                        }
                        questionObject.setQuestionItemObjects(itemObjects);
                    }

                    if (currentQuestionType == QuestionTypeEnum.MultipleChoice.getCode()) {
                        question.setCorrect(correctAnswer);
                    } else {
                        question.setCorrect(correctAnswer);
                    }
                } else if (currentQuestionType == QuestionTypeEnum.GapFilling.getCode() ||
                           currentQuestionType == QuestionTypeEnum.ShortAnswer.getCode()) {
                    question.setCorrect(correctAnswer);
                }

                questionObject.setAnalyze((String) questionData.get("analysis"));

                TextContent textContent = new TextContent();
                textContent.setContent(objectMapper.writeValueAsString(questionObject));
                textContent.setCreateTime(new Date());
                textContentMapper.insert(textContent);

                question.setInfoTextContentId(textContent.getId());

                questionMapper.insert(question);
                savedQuestionIds.add(question.getId());
                successCount++;

                logger.info("Saved question: id={}, type={}, subjectId={}", question.getId(), currentQuestionType, subjectId);

            } catch (Exception e) {
                failCount++;
                logger.error("Failed to save question: {}", questionData.get("question"), e);
            }
        }

        logger.info("Save questions completed: total={}, success={}, fail={}", questions.size(), successCount, failCount);
        return savedQuestionIds;
    }

    private String convertPdfToMarkdown(MultipartFile pdfFile) throws IOException {
        logger.info("Converting PDF to Markdown");

        String pdfToMarkdownPrompt = promptTemplateBuilder.buildPdfToMarkdownPrompt();

        File tempFile = File.createTempFile("upload_", ".pdf");
        tempFile.delete();
        try {
            pdfFile.transferTo(tempFile);
            String rawText = PdfToMarkdownUtil.convertPdfToMarkdown(tempFile);

            Map<String, String> request = new HashMap<>();
            request.put("content", rawText);
            request.put("prompt", pdfToMarkdownPrompt);

            String markdownContent = deepSeekApiClient.callChatCompletion(
                    pdfToMarkdownPrompt + "\n\n待转换内容：\n" + rawText);

            logger.info("PDF converted to Markdown successfully");
            return cleanMarkdownContent(markdownContent);

        } finally {
            tempFile.delete();
        }
    }

    private List<Map<String, Object>> generateQuestionsWithRetry(String markdownContent,
                                                                   Integer questionType, Integer questionCount, Integer difficulty, 
                                                                   Integer gradeLevel, Integer subjectId) throws Exception {
        int maxRetries = aiQuestionConfig.getMaxRetries();
        Exception lastException = null;
        double duplicateThreshold = 0.7; // 相似度阈值

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("Generating questions, attempt {}/{}", attempt, maxRetries);

                // 1. 构建包含工具调用提示的完整 Prompt
                String toolsPrompt = toolCallService.buildToolsPrompt();
                String questionPrompt = promptTemplateBuilder.buildQuestionGenerationPrompt(
                        markdownContent,
                        questionType != null ? questionType : aiQuestionConfig.getDefaultQuestionType(),
                        questionCount != null ? questionCount : aiQuestionConfig.getDefaultQuestionCount(),
                        difficulty != null ? difficulty : aiQuestionConfig.getDefaultDifficulty(),
                        gradeLevel);

                // 合并工具提示和题目生成提示
                String fullPrompt = toolsPrompt + "\n\n" + questionPrompt;

                String response = deepSeekApiClient.callChatCompletion(fullPrompt);

                // 2. 检查是否包含工具调用
                Map<String, Object> toolCall = toolCallService.parseToolCall(response);
                if (toolCall != null) {
                    String toolName = (String) toolCall.get("tool_name");
                    Map<String, Object> params = (Map<String, Object>) toolCall.get("parameters");
                    
                    logger.info("Executing tool call: {} with params: {}", toolName, params);
                    
                    // 执行工具调用
                    Map<String, Object> toolResult = toolCallService.executeTool(toolName, params);
                    
                    // 将工具执行结果反馈给大模型，继续生成
                    String feedbackPrompt = "工具执行结果:\n" + objectMapper.writeValueAsString(toolResult) + 
                                          "\n\n请根据以上查重结果，生成不重复的题目:";
                    response = deepSeekApiClient.callChatCompletion(feedbackPrompt);
                }

                List<Map<String, Object>> questions = parseAndValidateQuestions(response);

                if (!questions.isEmpty()) {
                    // 3. 本地查重过滤
                    List<Map<String, Object>> uniqueQuestions = filterDuplicateQuestions(questions, subjectId, duplicateThreshold);
                    logger.info("Filtered {} unique questions from {} generated", uniqueQuestions.size(), questions.size());
                    
                    if (!uniqueQuestions.isEmpty()) {
                        logger.info("Successfully generated {} unique questions on attempt {}", uniqueQuestions.size(), attempt);
                        return uniqueQuestions;
                    }
                    
                    logger.warn("All generated questions are duplicates, retrying...");
                } else {
                    logger.warn("No valid questions generated on attempt {}", attempt);
                }

            } catch (Exception e) {
                lastException = e;
                logger.error("Question generation failed on attempt {}/{}", attempt, maxRetries, e);

                if (attempt < maxRetries) {
                    Thread.sleep(aiQuestionConfig.getRetryDelayMs());
                }
            }
        }

        throw new Exception("Failed to generate questions after " + maxRetries + " attempts", lastException);
    }

    /**
     * 过滤重复题目
     */
    private List<Map<String, Object>> filterDuplicateQuestions(List<Map<String, Object>> questions, 
                                                               Integer subjectId, double threshold) {
        List<Map<String, Object>> uniqueQuestions = new ArrayList<>();
        
        for (Map<String, Object> question : questions) {
            String questionText = (String) question.get("question");
            
            try {
                // 检查是否重复
                boolean isDuplicate = questionDuplicateCheckService.isDuplicate(questionText, subjectId, threshold);
                
                if (!isDuplicate) {
                    uniqueQuestions.add(question);
                } else {
                    logger.debug("Skipping duplicate question: {}", questionText);
                }
            } catch (Exception e) {
                // 查重失败（如缺少 FULLTEXT 索引）时降级：不跳过，直接保留该题
                logger.warn("Duplicate check failed for question, keeping it: {}", e.getMessage());
                uniqueQuestions.add(question);
            }
        }
        
        return uniqueQuestions;
    }

    private List<Map<String, Object>> parseAndValidateQuestions(String response) throws IOException, ProcessingException {
        String jsonContent = extractJsonFromResponse(response);

        JsonNode rootNode = objectMapper.readTree(jsonContent);

        // 宽松模式：Schema 验证失败不直接抛异常，改为提取可解析的题目
        ProcessingReport report = questionSchema.validate(rootNode);
        if (!report.isSuccess()) {
            logger.warn("JSON Schema validation failed (non-fatal), will try lenient parse: {}", report);
        }

        List<Map<String, Object>> questions = new ArrayList<>();
        JsonNode questionsNode = rootNode.get("questions");

        if (questionsNode != null && questionsNode.isArray()) {
            for (JsonNode questionNode : questionsNode) {
                try {
                    if (!questionNode.has("question") || !questionNode.has("options") || !questionNode.has("correct_answer")) {
                        continue;
                    }

                    Map<String, Object> question = new HashMap<>();
                    question.put("question", questionNode.get("question").asText());

                    List<String> options = new ArrayList<>();
                    JsonNode optionsNode = questionNode.get("options");
                    if (optionsNode != null && optionsNode.isArray()) {
                        for (JsonNode option : optionsNode) {
                            options.add(option.asText());
                        }
                    }
                    if (options.size() < 2) {
                        continue;
                    }
                    question.put("options", options);

                    String correctAnswer = questionNode.get("correct_answer").asText().trim().toUpperCase();
                    question.put("correct_answer", correctAnswer);

                    // 兼容答案格式："A" / "A." / "A、" / "A,B" 等
                    boolean correctInOptions = options.stream()
                            .anyMatch(opt -> {
                                String optUpper = opt.trim().toUpperCase();
                                return optUpper.startsWith(correctAnswer) || optUpper.startsWith(correctAnswer.replaceAll("[,，、]", ""));
                            });
                    if (!correctInOptions) {
                        logger.warn("Correct answer {} not found in options, skipping question", correctAnswer);
                        continue;
                    }

                    question.put("analysis", questionNode.has("analysis") ? questionNode.get("analysis").asText() : "");
                    question.put("knowledge_point", questionNode.has("knowledge_point") ? questionNode.get("knowledge_point").asText() : "");
                    question.put("difficulty", questionNode.has("difficulty") ? questionNode.get("difficulty").asInt() : 2);

                    questions.add(question);
                } catch (Exception e) {
                    logger.warn("Failed to parse a question, skipping: {}", e.getMessage());
                }
            }
        }

        if (questions.isEmpty()) {
            throw new ProcessingException("No valid questions could be parsed from LLM response");
        }

        return questions;
    }

    /** 质量评估；调用失败（含 429 重试耗尽）时抛出异常，由调用方标记为 eval_failed，避免误判为质量不合格 */
    private double evaluateQuestionQuality(String originalContent, Map<String, Object> question) throws Exception {
        String judgePrompt = promptTemplateBuilder.buildJudgePrompt(
                originalContent,
                (String) question.get("question"),
                ((List<String>) question.get("options")).toArray(new String[0]),
                (String) question.get("correct_answer"),
                (String) question.get("analysis"));

        String response = deepSeekApiClient.callChatCompletion(judgePrompt);
        String jsonContent = extractJsonFromResponse(response);

        JsonNode jsonNode = objectMapper.readTree(jsonContent);
        if (jsonNode.has("overall_score")) {
            return jsonNode.get("overall_score").asDouble();
        }

        double factuality = jsonNode.has("factuality_score") ? jsonNode.get("factuality_score").asDouble() : 3.0;
        double relevance = jsonNode.has("relevance_score") ? jsonNode.get("relevance_score").asDouble() : 3.0;
        double difficulty = jsonNode.has("difficulty_alignment_score") ? jsonNode.get("difficulty_alignment_score").asDouble() : 3.0;

        return (factuality + relevance + difficulty) / 3.0;
    }

    private String extractJsonFromResponse(String response) {
        String cleaned = response.trim();

        int jsonStart = cleaned.indexOf("{");
        int jsonEnd = cleaned.lastIndexOf("}");

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return cleaned.substring(jsonStart, jsonEnd + 1);
        }

        int codeBlockStart = cleaned.indexOf("```json");
        if (codeBlockStart >= 0) {
            int contentStart = cleaned.indexOf("\n", codeBlockStart) + 1;
            int codeBlockEnd = cleaned.indexOf("```", contentStart);
            if (codeBlockEnd > contentStart) {
                return cleaned.substring(contentStart, codeBlockEnd).trim();
            }
        }

        return cleaned;
    }

    private String cleanMarkdownContent(String markdownContent) {
        String cleaned = markdownContent.trim();

        if (cleaned.startsWith("```markdown")) {
            cleaned = cleaned.substring("```markdown".length()).trim();
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3).trim();
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
        }

        return cleaned;
    }

    /**
     * 通过 ai-service（Spring AI）生成题目。
     * 与 legacy 流程保持相同返回结构，供 Vue 前端无感切换。
     */
    private Map<String, Object> generateQuestionsViaSpringAi(MultipartFile pdfFile, Integer subjectId,
            Integer gradeLevel, Integer questionType, Integer questionCount, Integer difficulty,
            Boolean enableQualityCheck, String taskId) throws Exception {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            updateProgress(taskId, "parsing", 0, 0, "正在解析 PDF 教材内容...");
            String markdownContent = convertPdfToMarkdown(pdfFile);
            result.put("markdownContent", markdownContent);

            updateProgress(taskId, "generating", 0, 0, "正在通过 Spring AI 生成题目...");

            // 映射 questionType 到 ai-service 的题型字符串
            String questionTypeStr = mapQuestionTypeToString(questionType);
            String difficultyStr = mapDifficultyToString(difficulty);
            String mode = Boolean.TRUE.equals(enableQualityCheck) ? "STANDARD" : "FAST";

            Map<String, Object> aiResult = springAiServiceClient.generateQuestions(
                    null, difficultyStr, questionCount, questionTypeStr, mode, markdownContent);

            // 提取 ai-service 返回的题目列表
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = (List<Map<String, Object>>) aiResult.getOrDefault("qualifiedQuestions",
                    new ArrayList<>());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rejected = (List<Map<String, Object>>) aiResult.getOrDefault("rejectedQuestions",
                    new ArrayList<>());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> evalFailed = (List<Map<String, Object>>) aiResult
                    .getOrDefault("evaluationFailedQuestions", new ArrayList<>());

            result.put("qualifiedQuestions", questions);
            result.put("rejectedQuestions", rejected);
            result.put("evaluationFailedQuestions", evalFailed);
            result.put("totalCount", questions.size() + rejected.size() + evalFailed.size());
            result.put("qualifiedCount", questions.size());
            result.put("rejectedCount", rejected.size());
            result.put("evaluationFailedCount", evalFailed.size());
            result.put("processingTime", System.currentTimeMillis() - startTime);
            result.put("title", aiResult.get("title"));

            updateProgress(taskId, "done",
                    questions.size() + rejected.size() + evalFailed.size(),
                    questions.size() + rejected.size() + evalFailed.size(), "生成完成");

            logger.info("Spring AI generation completed: total={}, qualified={}, rejected={}, evalFailed={}",
                    questions.size() + rejected.size() + evalFailed.size(), questions.size(), rejected.size(),
                    evalFailed.size());

        } catch (Exception e) {
            logger.error("Spring AI question generation failed", e);
            updateProgress(taskId, "failed", 0, 0, "生成失败：" + e.getMessage());
            result.put("error", e.getMessage());
            throw e;
        }
        return result;
    }

    private String mapQuestionTypeToString(Integer questionType) {
        if (questionType == null) {
            return "single";
        }
        switch (questionType) {
            case 1: return "single";
            case 2: return "multiple";
            case 3: return "judge";
            case 4: return "gap";
            case 5: return "short";
            default: return "single";
        }
    }

    private String mapDifficultyToString(Integer difficulty) {
        if (difficulty == null) {
            return "medium";
        }
        switch (difficulty) {
            case 1: return "easy";
            case 2: return "medium";
            case 3: return "hard";
            default: return "medium";
        }
    }
}
