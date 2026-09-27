package com.mindskip.xzs.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool Calling 服务 - 处理大模型的工具调用请求
 */
@Service
public class ToolCallService {

    private static final Logger logger = LoggerFactory.getLogger(ToolCallService.class);

    @Autowired
    private QuestionDuplicateCheckService questionDuplicateCheckService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定义可用的工具列表
     */
    public Map<String, Object> getAvailableTools() {
        Map<String, Object> tools = new HashMap<>();
        
        // 题库查重工具
        Map<String, Object> checkDuplicateTool = new HashMap<>();
        checkDuplicateTool.put("name", "checkDuplicate");
        checkDuplicateTool.put("description", "检查题库中是否存在相似题目，用于避免生成重复试题。在生成题目之前调用此工具进行查重。");
        
        Map<String, Object> checkDuplicateParams = new HashMap<>();
        Map<String, Object> keywordsParam = new HashMap<>();
        keywordsParam.put("type", "string");
        keywordsParam.put("description", "题目关键词或题干文本，用于在题库中搜索相似题目");
        checkDuplicateParams.put("keywords", keywordsParam);
        
        Map<String, Object> subjectIdParam = new HashMap<>();
        subjectIdParam.put("type", "integer");
        subjectIdParam.put("description", "学科ID，可选参数，用于限定搜索范围");
        checkDuplicateParams.put("subjectId", subjectIdParam);
        
        checkDuplicateTool.put("parameters", checkDuplicateParams);
        tools.put("checkDuplicate", checkDuplicateTool);
        
        return tools;
    }

    /**
     * 执行工具调用
     * @param toolName 工具名称
     * @param parameters 工具参数
     * @return 工具执行结果
     */
    public Map<String, Object> executeTool(String toolName, Map<String, Object> parameters) {
        logger.info("Executing tool: {}, parameters: {}", toolName, parameters);
        
        try {
            switch (toolName) {
                case "checkDuplicate":
                    return executeCheckDuplicate(parameters);
                default:
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "Unknown tool: " + toolName);
                    return error;
            }
        } catch (Exception e) {
            logger.error("Tool execution failed", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }

    /**
     * 执行查重工具
     */
    private Map<String, Object> executeCheckDuplicate(Map<String, Object> parameters) {
        String keywords = (String) parameters.get("keywords");
        Integer subjectId = parameters.get("subjectId") != null ? ((Number) parameters.get("subjectId")).intValue() : null;
        
        return questionDuplicateCheckService.checkDuplicate(keywords, subjectId);
    }

    /**
     * 生成工具调用的 Prompt 描述
     */
    public String buildToolsPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("## 可用工具\n\n");
        
        Map<String, Object> tools = getAvailableTools();
        for (Map.Entry<String, Object> entry : tools.entrySet()) {
            Map<String, Object> tool = (Map<String, Object>) entry.getValue();
            sb.append("### ").append(tool.get("name")).append("\n");
            sb.append("- 描述: ").append(tool.get("description")).append("\n");
            sb.append("- 参数:\n");
            
            Map<String, Object> params = (Map<String, Object>) tool.get("parameters");
            for (Map.Entry<String, Object> paramEntry : params.entrySet()) {
                Map<String, Object> param = (Map<String, Object>) paramEntry.getValue();
                sb.append("  - ").append(paramEntry.getKey()).append(" (").append(param.get("type")).append("): ")
                  .append(param.get("description")).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("## 工具调用格式\n");
        sb.append("当你需要调用工具时，请在回答中使用以下格式：\n");
        sb.append("```tool_call\n");
        sb.append("{\n");
        sb.append("  \"tool_name\": \"工具名称\",\n");
        sb.append("  \"parameters\": {\n");
        sb.append("    \"参数名\": \"参数值\"\n");
        sb.append("  }\n");
        sb.append("}\n");
        sb.append("```\n\n");
        sb.append("注意：只有在你确定需要调用工具时才使用此格式，不要滥用工具调用。\n");
        
        return sb.toString();
    }

    /**
     * 解析工具调用响应
     * @param response 大模型响应
     * @return 工具调用信息，如果不是工具调用则返回 null
     */
    public Map<String, Object> parseToolCall(String response) {
        String trimmed = response.trim();
        
        int toolCallStart = trimmed.indexOf("```tool_call");
        if (toolCallStart < 0) {
            return null;
        }
        
        int contentStart = trimmed.indexOf("\n", toolCallStart) + 1;
        int toolCallEnd = trimmed.indexOf("```", contentStart);
        
        if (toolCallEnd < contentStart) {
            return null;
        }
        
        try {
            String jsonContent = trimmed.substring(contentStart, toolCallEnd).trim();
            return objectMapper.readValue(jsonContent, Map.class);
        } catch (Exception e) {
            logger.error("Failed to parse tool call", e);
            return null;
        }
    }
}