# AI Agent 端到端测试

本目录包含 AI 智能出题模块的自动化测试脚本。

## 一、Selenium E2E 测试

覆盖完整闭环：管理员登录 → AI 自然语言出题 → 四步过程展示 → 题目生成 → 保存题库。

### 运行

```bash
# 1. 确保后端(8000)、管理端前端(8002)已启动
# 2. 安装依赖
pip install -r requirements.txt
# 3. 运行测试
python test_ai_agent_e2e.py
```

测试会自动打开 Chrome，执行完整流程并截图（失败时保存 `ai_agent_e2e_fail.png`）。

## 二、JMeter 接口压力测试

测试三个核心接口：登录、AI 生成试卷、提交考试。

### 运行

```bash
# 命令行模式（需安装 JMeter 5.x）
jmeter -n -t ai_exam_api_test.jmx -l result.jtl -e -o report/

# 或 GUI 模式
jmeter -t ai_exam_api_test.jmx
```

### 参数配置

在 `ai_exam_api_test.jmx` 顶部「用户定义变量」中修改：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| HOST | localhost | 后端地址 |
| PORT | 8000 | 后端端口 |
| THREADS | 10 | 并发线程数 |
| RAMPUP | 10 | 启动时长(秒) |
| LOOPS | 1 | 循环次数 |

## 三、异常测试

通过 JMeter / Postman 验证以下异常场景的系统稳定性：

| 场景 | 接口 | 预期 |
|------|------|------|
| AI 超时 | POST /api/admin/ai-question/generate-by-text | 触发规则解析回退 + 兜底题库，不抛 500 |
| 空参数 | POST /api/admin/ai-question/generate-by-text (body 空) | 返回参数错误提示，不崩溃 |
| JSON 格式错误 | 传入非法 requirementText | RequirementParser 规则解析兜底 |
| 网络异常 | 模拟 LLM 不可达 | 重试 3 次后加载 DemoQuestionBank |
| 非空答卷评测 | GET /api/student/exampaper/answer/evaluate/{id} | 返回正确率/薄弱点/建议 |
| 不存在的答卷 | evaluate/999999 | 返回业务错误码，不报 500 |
