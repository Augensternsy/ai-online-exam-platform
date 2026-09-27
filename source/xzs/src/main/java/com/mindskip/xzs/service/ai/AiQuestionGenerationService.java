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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AiQuestionGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(AiQuestionGenerationService.class);

    @Autowired
    private AiQuestionConfig aiQuestionConfig;

    @Autowired
    private DeepSeekApiClient deepSeekApiClient;

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
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

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
                                                         Boolean enableQualityCheck) throws Exception {
        logger.info("Starting AI question generation from PDF, qualityCheck={}", enableQualityCheck);

        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();

        try {
            String markdownContent = convertPdfToMarkdown(pdfFile);
            result.put("markdownContent", markdownContent);

            List<Map<String, Object>> generatedQuestions = generateQuestionsWithRetry(
                    markdownContent, questionType, questionCount, difficulty, gradeLevel, subjectId);

            List<Map<String, Object>> qualifiedQuestions = new ArrayList<>();
            List<Map<String, Object>> rejectedQuestions = new ArrayList<>();

            if (enableQualityCheck != null && enableQualityCheck) {
                logger.info("Starting parallel quality evaluation for {} questions", generatedQuestions.size());
                long evalStartTime = System.currentTimeMillis();

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
                                logger.error("Failed to evaluate question quality", e);
                                question.put("qualityScore", 3.0);
                                question.put("status", "rejected");
                            }
                            return question;
                        }, executorService))
                        .collect(Collectors.toList());

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                for (CompletableFuture<Map<String, Object>> future : futures) {
                    Map<String, Object> question = future.get();
                    if ("qualified".equals(question.get("status"))) {
                        qualifiedQuestions.add(question);
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
            result.put("totalCount", generatedQuestions.size());
            result.put("qualifiedCount", qualifiedQuestions.size());
            result.put("rejectedCount", rejectedQuestions.size());
            result.put("processingTime", System.currentTimeMillis() - startTime);

            logger.info("Question generation completed: total={}, qualified={}, rejected={}, time={}ms",
                    generatedQuestions.size(), qualifiedQuestions.size(), rejectedQuestions.size(),
                    System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            logger.error("Question generation failed", e);
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
                question.setScore(1000);
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
                            item.setScore(1000);
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

    private double evaluateQuestionQuality(String originalContent, Map<String, Object> question) {
        try {
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

        } catch (Exception e) {
            logger.error("Question quality evaluation failed", e);
            return 3.0;
        }
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
}
