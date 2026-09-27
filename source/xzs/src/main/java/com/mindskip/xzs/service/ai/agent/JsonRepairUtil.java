package com.mindskip.xzs.service.ai.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LLM 返回的 JSON 经常存在格式问题（多余逗号、未闭合引号、Markdown 代码块包裹等）。
 * 本工具提供稳健的 JSON 提取与轻量修复能力，尽量把"接近正确"的输出解析成对象。
 */
public final class JsonRepairUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonRepairUtil.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonRepairUtil() {}

    /**
     * 从 LLM 原始响应中提取并解析 JSON 对象。
     * 处理顺序：去掉 Markdown 代码块 -> 截取首个 { 到末个 } -> 直接解析 -> 修复后再解析。
     */
    public static JsonNode parse(String raw) {
        if (raw == null) return null;
        String text = raw.trim();

        // 1. 去除 ```json ... ``` 或 ``` ... ``` 包裹
        int fenceStart = text.indexOf("```");
        if (fenceStart >= 0) {
            int contentStart = text.indexOf('\n', fenceStart);
            if (contentStart > 0) {
                int fenceEnd = text.indexOf("```", contentStart);
                if (fenceEnd > contentStart) {
                    text = text.substring(contentStart, fenceEnd).trim();
                }
            }
        }

        // 2. 截取首个 '{' 到末个 '}'
        int first = text.indexOf('{');
        int last = text.lastIndexOf('}');
        if (first >= 0 && last > first) {
            text = text.substring(first, last + 1);
        }

        // 3. 直接解析
        try {
            return MAPPER.readTree(text);
        } catch (Exception e) {
            logger.debug("Direct JSON parse failed, attempting repair: {}", e.getMessage());
        }

        // 4. 轻量修复：去除尾随逗号、统一引号
        String repaired = text
                .replaceAll(",\\s*([}\\]])", "$1")   // 去除对象/数组末尾多余逗号
                .replaceAll("\\s+", " ")
                .trim();
        try {
            return MAPPER.readTree(repaired);
        } catch (Exception e) {
            logger.warn("JSON repair failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 安全获取 JsonNode 文本值，缺失或非文本返回默认值。
     */
    public static String text(JsonNode node, String field, String defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) return defaultValue;
        return node.get(field).asText(defaultValue);
    }

    public static int intVal(JsonNode node, String field, int defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) return defaultValue;
        return node.get(field).asInt(defaultValue);
    }
}
