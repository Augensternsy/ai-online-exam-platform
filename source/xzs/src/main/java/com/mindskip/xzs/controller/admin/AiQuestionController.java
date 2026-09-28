package com.mindskip.xzs.controller.admin;

import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.domain.User;
import com.mindskip.xzs.service.ai.AiQuestionGenerationService;
import com.mindskip.xzs.service.ai.agent.ExamAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("AdminAiQuestionController")
@RequestMapping(value = "/api/admin/ai-question")
public class AiQuestionController extends BaseApiController {

    private static final Logger logger = LoggerFactory.getLogger(AiQuestionController.class);

    @Autowired
    private AiQuestionGenerationService aiQuestionGenerationService;

    @Autowired
    private ExamAgent examAgent;

    @RequestMapping(value = "/generate", method = RequestMethod.POST)
    public RestResponse<Map<String, Object>> generateQuestions(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "gradeLevel", required = false) Integer gradeLevel,
            @RequestParam(value = "questionType", required = false) Integer questionType,
            @RequestParam(value = "questionCount", required = false) Integer questionCount,
            @RequestParam(value = "difficulty", required = false) Integer difficulty,
            @RequestParam(value = "enableQualityCheck", required = false, defaultValue = "true") Boolean enableQualityCheck,
            @RequestParam(value = "taskId", required = false) String taskId) {

        try {
            logger.info("Received AI question generation request: file={}, subjectId={}, questionType={}, questionCount={}, enableQualityCheck={}",
                    file.getOriginalFilename(), subjectId, questionType, questionCount, enableQualityCheck);

            if (file.isEmpty()) {
                return RestResponse.fail("请上传 PDF 文件");
            }

            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
                return RestResponse.fail("请上传 PDF 格式的文件");
            }

            Map<String, Object> result = aiQuestionGenerationService.generateQuestionsFromPdf(
                    file, subjectId, gradeLevel, questionType, questionCount, difficulty, enableQualityCheck, taskId);

            return RestResponse.ok(result);

        } catch (Exception e) {
            logger.error("AI question generation failed", e);
            return RestResponse.fail("题目生成失败：" + e.getMessage());
        }
    }

    /** 查询生成任务进度（前端轮询，用于展示评估进度与限流重试状态） */
    @RequestMapping(value = "/progress", method = RequestMethod.GET)
    public RestResponse<Map<String, Object>> progress(@RequestParam("taskId") String taskId) {
        Map<String, Object> progress = aiQuestionGenerationService.getProgress(taskId);
        if (progress == null) {
            Map<String, Object> unknown = new HashMap<>();
            unknown.put("stage", "unknown");
            return RestResponse.ok(unknown);
        }
        return RestResponse.ok(progress);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public RestResponse<List<Integer>> saveQuestions(
            @RequestBody Map<String, Object> request) {

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions = (List<Map<String, Object>>) request.get("questions");
            Integer subjectId = (Integer) request.get("subjectId");
            Integer gradeLevel = (Integer) request.get("gradeLevel");
            Integer questionType = (Integer) request.get("questionType");

            if (questions == null || questions.isEmpty()) {
                return RestResponse.fail("没有可保存的题目");
            }

            User currentUser = getCurrentUser();
            List<Integer> savedIds = aiQuestionGenerationService.saveQuestionsToDatabase(
                    questions, subjectId, gradeLevel, questionType, currentUser.getId());

            return RestResponse.ok(savedIds);

        } catch (Exception e) {
            logger.error("Failed to save questions", e);
            return RestResponse.fail("题目保存失败：" + e.getMessage());
        }
    }

    /**
     * 自然语言出题（Exam Agent 入口）。
     * 用户输入一句话需求，Agent 自动解析科目/难度/题量/题型并生成结构化试卷。
     */
    @RequestMapping(value = "/generate-by-text", method = RequestMethod.POST)
    public RestResponse<Map<String, Object>> generateByText(@RequestBody Map<String, Object> request) {
        try {
            String input = request.get("input") == null ? "" : request.get("input").toString().trim();
            if (input.isEmpty()) {
                return RestResponse.fail("请输入出题需求");
            }
            logger.info("Exam Agent generate-by-text: {}", input);
            Map<String, Object> result = examAgent.generateFromText(input);
            return RestResponse.ok(result);
        } catch (Exception e) {
            logger.error("Exam Agent generate-by-text failed", e);
            return RestResponse.fail("AI 出题失败：" + e.getMessage());
        }
    }
}
