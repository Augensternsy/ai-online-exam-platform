package com.mindskip.xzs.service.ai.agent;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 任务规划模块：根据 ExamRequirement 生成两类 Prompt ——
 * 1. 需求解析 Prompt（让 LLM 把自然语言转成结构化 ExamRequirement）
 * 2. 出题 Prompt（支持多题型比例、知识点、难度）
 */
@Component
public class PromptPlanner {

    /**
     * 构造「自然语言 -> 结构化需求」的解析 Prompt。
     */
    public String buildRequirementParsePrompt(String userInput) {
        return "你是一个考试需求解析助手。请从用户的自然语言描述中提取考试出题需求，" +
                "并严格按照下方 JSON 格式返回，不要输出任何额外说明。\n\n" +
                "用户输入：\n" + userInput + "\n\n" +
                "输出 JSON 格式：\n" +
                "```json\n" +
                "{\n" +
                "  \"subject\": \"科目名称，如 Java\",\n" +
                "  \"difficulty\": 2,\n" +
                "  \"questionCount\": 20,\n" +
                "  \"questionTypes\": [1, 2, 3],\n" +
                "  \"knowledgePoints\": [\"知识点1\", \"知识点2\"]\n" +
                "}\n" +
                "```\n\n" +
                "字段说明：\n" +
                "- difficulty：1=简单，2=中等，3=困难（用户未提及时默认 2）\n" +
                "- questionTypes：题型数组，1=单选题，2=多选题，3=判断题（用户未指定时默认 [1]）\n" +
                "- questionCount：题目总数（用户未提及时默认 10）\n" +
                "- knowledgePoints：用户提到的具体知识点，未提到则为空数组 []\n" +
                "- subject：科目名称，必须从用户输入中提取，不要臆造";
    }

    /**
     * 构造「按需求生成题目」的出题 Prompt，支持多题型混合。
     */
    public String buildQuestionGenerationPrompt(ExamRequirement req) {
        String subject = req.getSubject() != null ? req.getSubject() : "通用";
        int difficulty = req.getDifficulty() != null ? req.getDifficulty() : 2;
        int count = req.getQuestionCount() != null ? req.getQuestionCount() : 10;
        List<Integer> types = req.getQuestionTypes() != null && !req.getQuestionTypes().isEmpty()
                ? req.getQuestionTypes() : java.util.Collections.singletonList(1);

        String typeDesc = types.stream().map(this::typeName).collect(Collectors.joining("、"));
        String knowledge = (req.getKnowledgePoints() != null && !req.getKnowledgePoints().isEmpty())
                ? String.join("、", req.getKnowledgePoints()) : subject + "核心知识";

        return "你是一位资深的" + subject + "学科命题专家。请根据以下需求生成一套高质量考试题目。\n\n" +
                "## 出题需求\n" +
                "- 科目：" + subject + "\n" +
                "- 难度：" + difficultyName(difficulty) + "\n" +
                "- 题目总数：" + count + " 道\n" +
                "- 题型：" + typeDesc + "（各题型数量尽量平均分配，总数必须等于 " + count + "）\n" +
                "- 知识点：" + knowledge + "\n\n" +
                "## 输出要求\n" +
                "严格按以下 JSON 格式返回，不要输出任何额外文字、解释或 Markdown 代码块标记：\n\n" +
                "{\n" +
                "  \"title\": \"" + subject + "能力测试\",\n" +
                "  \"questions\": [\n" +
                "    {\n" +
                "      \"type\": 1,\n" +
                "      \"question\": \"题目题干\",\n" +
                "      \"options\": [\"选项A内容\", \"选项B内容\", \"选项C内容\", \"选项D内容\"],\n" +
                "      \"answer\": \"A\",\n" +
                "      \"analysis\": \"答案解析\",\n" +
                "      \"knowledgePoint\": \"考查知识点\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n\n" +
                "## 字段约束\n" +
                "1. type：1=单选题（4个选项），2=多选题（4个选项，answer 用逗号分隔如 \"A,B,C\"），3=判断题（options 固定为 [\"正确\",\"错误\"]，answer 为 \"A\" 表示正确、\"B\" 表示错误）\n" +
                "2. options：单选/多选必须恰好 4 个，判断题恰好 2 个\n" +
                "3. answer：单选为单个字母 A-D；多选为逗号分隔字母；判断为 A 或 B\n" +
                "4. analysis：必须解释为什么答案正确\n" +
                "5. questions 数组长度必须等于 " + count + "\n" +
                "6. 题目必须严谨、准确、有区分度，不能出现事实性错误\n\n" +
                "现在请直接输出 JSON：";
    }

    private String typeName(int t) {
        switch (t) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            default: return "单选题";
        }
    }

    private String difficultyName(int d) {
        switch (d) {
            case 1: return "简单";
            case 2: return "中等";
            case 3: return "困难";
            default: return "中等";
        }
    }
}
