package com.mindskip.xzs.utility;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 轻量级文本相似度计算工具类
 * 采用 2-gram 字符切分 + 余弦相似度方案
 * 后续可升级为 BM25、Embedding 或向量数据库
 */
public class TextSimilarityUtil {

    private static final Pattern PUNCTUATION = Pattern.compile("[\\p{Punct}\\s]+");
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern JSON_KEY = Pattern.compile("\"[^\"]+\":");
    private static final Pattern JSON_BRACES = Pattern.compile("[{}]");
    private static final Pattern JSON_BRACKETS = Pattern.compile("[\\[\\]]");
    private static final Pattern JSON_QUOTES = Pattern.compile("\"");
    private static final Pattern EXTRA_WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern NULL_LITERAL = Pattern.compile("\\bnull\\b");
    private static final Pattern NULL_REPEAT = Pattern.compile("(null)+");
    private static final Pattern JSON_VALUE = Pattern.compile("(question|answer|option|content|分析|解答|答案|解析):", Pattern.CASE_INSENSITIVE);
    private static final Pattern COMMA_MULTI_SPACE = Pattern.compile(",\\s*");
    private static final Pattern JSON_EMPTY_ITEM = Pattern.compile(",\\s*,|,\\s*}");

    public static String preprocess(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String clean = HtmlUtil.clear(text);
        clean = JSON_KEY.matcher(clean).replaceAll("");
        clean = JSON_BRACES.matcher(clean).replaceAll("");
        clean = JSON_BRACKETS.matcher(clean).replaceAll("");
        clean = JSON_QUOTES.matcher(clean).replaceAll("");
        clean = JSON_VALUE.matcher(clean).replaceAll("");
        clean = PUNCTUATION.matcher(clean).replaceAll("");
        clean = NULL_LITERAL.matcher(clean).replaceAll("");
        clean = NULL_REPEAT.matcher(clean).replaceAll(" ");
        clean = COMMA_MULTI_SPACE.matcher(clean).replaceAll(" ");
        clean = JSON_EMPTY_ITEM.matcher(clean).replaceAll(",");
        clean = EXTRA_WHITESPACE.matcher(clean).replaceAll(" ").trim();
        return clean.toLowerCase();
    }

    public static String cleanForDisplay(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        String clean = HtmlUtil.clear(content);
        clean = JSON_KEY.matcher(clean).replaceAll("");
        clean = JSON_BRACES.matcher(clean).replaceAll("");
        clean = JSON_BRACKETS.matcher(clean).replaceAll("");
        clean = NULL_REPEAT.matcher(clean).replaceAll(" ");
        clean = NULL_LITERAL.matcher(clean).replaceAll("");
        clean = COMMA_MULTI_SPACE.matcher(clean).replaceAll(" ");
        clean = JSON_EMPTY_ITEM.matcher(clean).replaceAll(",");
        clean = clean.replaceAll("\\b(\\d+)\\b", "$1");
        clean = clean.replaceAll("\"", "");
        clean = EXTRA_WHITESPACE.matcher(clean).replaceAll(" ").trim();
        clean = formatQuestionOptions(clean);
        return clean.trim();
    }

    private static String formatQuestionOptions(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        text = text.replaceAll("([A-Za-z])\\s*,", "$1.");
        text = text.replaceAll(",\\s*([A-Za-z])\\s*,", ", $1.");
        text = text.replaceAll(",\\s*([A-Za-z])\\s*$", ", $1");
        text = text.replaceAll("^\\s*,\\s*", "");
        text = text.replaceAll("\\s*,\\s*,\\s*", ", ");
        text = text.replaceAll("\\s+", " ");
        return text;
    }

    /**
     * 从内容中提取题干文本
     * 当前采用轻量级文本抽取，后续可升级为结构化题干解析或 Embedding
     */
    public static String extractQuestionText(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        return preprocess(content);
    }

    /**
     * 2-gram 字符切分（中文适用）
     */
    public static List<String> biGramSplit(String text) {
        List<String> grams = new ArrayList<>();
        text = preprocess(text);
        if (text.length() < 2) {
            if (!text.isEmpty()) {
                grams.add(text);
            }
            return grams;
        }
        for (int i = 0; i <= text.length() - 2; i++) {
            grams.add(text.substring(i, i + 2));
        }
        return grams;
    }

    /**
     * 计算词频向量
     */
    public static Map<String, Integer> getTermFrequency(List<String> tokens) {
        Map<String, Integer> tf = new HashMap<>();
        for (String token : tokens) {
            tf.put(token, tf.getOrDefault(token, 0) + 1);
        }
        return tf;
    }

    /**
     * 余弦相似度计算
     */
    public static double cosineSimilarity(String text1, String text2) {
        List<String> tokens1 = biGramSplit(text1);
        List<String> tokens2 = biGramSplit(text2);

        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        Map<String, Integer> tf1 = getTermFrequency(tokens1);
        Map<String, Integer> tf2 = getTermFrequency(tokens2);

        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(tf1.keySet());
        allTerms.addAll(tf2.keySet());

        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (String term : allTerms) {
            int v1 = tf1.getOrDefault(term, 0);
            int v2 = tf2.getOrDefault(term, 0);
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 安全转换为 Integer
     */
    public static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Byte) {
            return ((Byte) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 保留两位小数
     */
    public static Double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}