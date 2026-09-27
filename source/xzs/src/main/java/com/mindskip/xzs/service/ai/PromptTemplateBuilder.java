package com.mindskip.xzs.service.ai;

import org.springframework.stereotype.Component;

@Component
public class PromptTemplateBuilder {

    public String buildPdfToMarkdownPrompt() {
        return "你是一个专业的教材内容解析专家。请将以下从 PDF 提取的原始文本转换为结构化的 Markdown 格式。\n\n" +
               "要求：\n" +
               "1. 过滤掉页码、页脚、水印等干扰信息\n" +
               "2. 识别并保留三级目录结构（使用 #、##、### 标记标题）\n" +
               "3. 将列表项转换为 Markdown 列表格式（使用 - 标记）\n" +
               "4. 数学公式转换为 LaTeX 格式（使用 $ 包裹）\n" +
               "5. 保持原文的知识脉络和逻辑结构\n" +
               "6. 去除多余的空白行和重复内容\n\n" +
               "请直接输出 Markdown 内容，不要添加任何解释说明。";
    }

    public String buildQuestionGenerationPrompt(String markdownContent, int questionType, int questionCount, int difficulty, Integer gradeLevel) {
        String questionTypeName = getQuestionTypeName(questionType);
        String difficultyName = getDifficultyName(difficulty);
        String gradeLevelName = gradeLevel != null ? getGradeLevelName(gradeLevel) : "通用";

        return "你是一个资深学科专家和考试命题专家。请根据以下教材内容，生成高质量的考试题目。\n\n" +
               "## 任务指令\n" +
               "根据提供的 Markdown 格式教材内容，生成 " + questionCount + " 道" + questionTypeName + "。\n\n" +
               "## 上下文设定\n" +
               "- 角色：资深学科专家，具备丰富的教学经验和命题经验\n" +
               "- 场景：正规考试场景，题目需要严谨、准确、有区分度\n" +
               "- 目标年级：" + gradeLevelName + "\n" +
               "- 难度级别：" + difficultyName + "\n\n" +
               "## 输入数据\n" +
               "以下是教材的 Markdown 格式内容：\n\n" +
               markdownContent + "\n\n" +
               "## 输出要求\n" +
               "请严格按照以下 JSON 格式返回，不要添加任何额外内容：\n\n" +
               "```json\n" +
               "{\n" +
               "  \"questions\": [\n" +
               "    {\n" +
               "      \"question\": \"题目题干内容\",\n" +
               "      \"options\": [\"A. 选项A内容\", \"B. 选项B内容\", \"C. 选项C内容\", \"D. 选项D内容\"],\n" +
               "      \"correct_answer\": \"A\",\n" +
               "      \"analysis\": \"详细的答案解析，说明为什么选这个答案\",\n" +
               "      \"knowledge_point\": \"本题考查的知识点\",\n" +
               "      \"difficulty\": " + difficulty + "\n" +
               "    }\n" +
               "  ]\n" +
               "}\n" +
               "```\n\n" +
               "## 注意事项\n" +
               "1. 题目必须基于提供的教材内容，不能凭空捏造\n" +
               "2. 选项要有干扰性，但不能有歧义\n" +
               "3. 正确答案必须明确且唯一\n" +
               "4. 解析要详细，说明答题思路\n" +
               "5. 每道题考查一个独立的知识点\n" +
               "6. 难度要符合设定的级别\n" +
               "7. 题目深度和表述方式要符合" + gradeLevelName + "学生的认知水平\n\n" +
               "## 示例\n\n" +
               "输入教材内容：\n" +
               "## 光合作用的过程\n" +
               "光合作用分为光反应和暗反应两个阶段。光反应发生在叶绿体的类囊体膜上，需要光照，产生 ATP 和 NADPH。暗反应发生在叶绿体基质中，不需要光照，利用光反应产生的 ATP 和 NADPH 将 CO2 转化为有机物。\n\n" +
               "输出：\n" +
               "```json\n" +
               "{\n" +
               "  \"questions\": [\n" +
               "    {\n" +
               "      \"question\": \"光合作用的光反应阶段发生在叶绿体的哪个部位？\",\n" +
               "      \"options\": [\"A. 叶绿体基质\", \"B. 类囊体膜\", \"C. 叶绿体外膜\", \"D. 叶绿体内膜\"],\n" +
               "      \"correct_answer\": \"B\",\n" +
               "      \"analysis\": \"光反应发生在叶绿体的类囊体膜上，该膜上含有光合色素和电子传递链，能够吸收光能并转化为化学能，产生 ATP 和 NADPH。叶绿体基质是暗反应的场所。\",\n" +
               "      \"knowledge_point\": \"光合作用的场所\",\n" +
               "      \"difficulty\": 2\n" +
               "    }\n" +
               "  ]\n" +
               "}\n" +
               "```";
    }

    public String buildJudgePrompt(String originalContent, String question, String[] options, String correctAnswer, String analysis) {
        return "你是一个严格的试题质量评审专家。请对以下 AI 生成的试题进行质量评估。\n\n" +
               "## 原始教材内容\n" +
               originalContent + "\n\n" +
               "## 待评审试题\n" +
               "题目：" + question + "\n" +
               "选项：" + String.join(", ", options) + "\n" +
               "正确答案：" + correctAnswer + "\n" +
               "解析：" + analysis + "\n\n" +
               "## 评审维度\n" +
               "请从以下三个维度进行评分（1-5分，5分为最高）：\n\n" +
               "1. **忠实度（Factuality）**：题目内容是否准确反映了教材内容，是否存在事实性错误或 AI 幻觉\n" +
               "2. **相关性（Relevance）**：题目是否与教材知识点紧密相关，是否考查了重要知识点\n" +
               "3. **难度一致性（Difficulty Alignment）**：题目难度是否与设定难度级别一致\n\n" +
               "## 输出格式\n" +
               "请严格按照以下 JSON 格式返回：\n\n" +
               "```json\n" +
               "{\n" +
               "  \"factuality_score\": 4,\n" +
               "  \"relevance_score\": 5,\n" +
               "  \"difficulty_alignment_score\": 4,\n" +
               "  \"overall_score\": 4.3,\n" +
               "  \"comments\": \"简要评价意见\"\n" +
               "}\n" +
               "```\n\n" +
               "注意：overall_score 是三个维度的平均分，保留一位小数。";
    }

    private String getQuestionTypeName(int questionType) {
        switch (questionType) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            case 5: return "简答题";
            default: return "单选题";
        }
    }

    private String getDifficultyName(int difficulty) {
        switch (difficulty) {
            case 1: return "简单（基础概念识别）";
            case 2: return "中等（知识理解和应用）";
            case 3: return "困难（综合分析和推理）";
            default: return "中等（知识理解和应用）";
        }
    }

    private String getGradeLevelName(int gradeLevel) {
        switch (gradeLevel) {
            case 1: return "大一";
            case 2: return "大二";
            case 3: return "大三";
            case 4: return "大四";
            case 5: return "研一";
            case 6: return "研二";
            case 7: return "研三";
            default: return "通用";
        }
    }
}
