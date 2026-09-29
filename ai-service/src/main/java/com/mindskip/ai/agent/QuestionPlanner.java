package com.mindskip.ai.agent;

import org.springframework.stereotype.Component;

/** 步骤二：任务规划 —— 构造结构化出题 Prompt。 */
@Component
public class QuestionPlanner {

    public String buildPrompt(RequirementAnalyzer.AnalyzedRequirement req) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位专业的试题命题专家。请根据以下要求生成高质量试题。\n\n");
        sb.append("【科目】").append(req.getSubject()).append("\n");
        sb.append("【难度】").append(difficultyLabel(req.getDifficulty())).append("\n");
        sb.append("【题量】").append(req.getQuestionCount()).append(" 道题\n");
        sb.append("【题型】").append(String.join("、", req.getQuestionTypes())).append("\n");
        if (req.getContent() != null && !req.getContent().isBlank()) {
            sb.append("【教材背景】\n").append(truncate(req.getContent(), 4000)).append("\n");
        }
        sb.append("\n【要求】\n");
        sb.append("1. 每题包含：question(题干)、options(选项数组，至少2个)、correct_answer(正确选项字母，多选用逗号分隔)、")
          .append("analysis(解析)、difficulty(1-3)、knowledge_point(知识点)。\n");
        sb.append("2. 题目必须基于教材背景，事实准确，无歧义。\n");
        sb.append("3. 判断题（judge）的 options 固定为 [\"正确\", \"错误\"]，correct_answer 为 \"A\" 或 \"B\"。\n");
        sb.append("4. 返回严格的 JSON，不要包含 markdown 代码块或其他多余文本。\n");
        return sb.toString();
    }

    private String difficultyLabel(String d) {
        switch (d) {
            case "easy": return "简单";
            case "hard": return "困难";
            default: return "中等";
        }
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
