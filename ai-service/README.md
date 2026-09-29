# ai-service — Spring AI 微服务

独立的 AI 出题微服务，基于 **JDK 17 + Spring Boot 3.2.5 + Spring AI 1.0.0-M6**，通过 Spring AI `ChatClient` 调用 DeepSeek（OpenAI 兼容接口），与主系统（Spring Boot 2.1.6 / JDK 8）解耦部署。

## 架构位置

```
Vue 前端
  ↓
Spring Boot 主系统（8000，JDK8）
  ↓  ai.provider=spring-ai 时
SpringAiServiceClient（HTTP）
  ↓
ai-service（本服务，8081）
  ↓
Spring AI ChatClient
  ↓
DeepSeek API
```

> 主系统保留原 DeepSeekApiClient（`ai.provider=legacy`）可随时回退；本服务是可选的 Spring AI 接入层。

## 接口

| 方法 | 路径 | 说明 |
| ---- | ---- | ---- |
| POST | `/api/ai/chat` | 基础对话，`{"prompt":"..."}` → `{"content":"..."}` |
| POST | `/api/ai/questions/generate` | AI 出题（Agent 四步流程：需求解析 → 规划 → 生成 → 质量评估） |
| GET | `/api/ai/status` | 限流状态查询 |
| GET | `/actuator/health` | 健康检查 |

出题请求示例：

```json
{
  "subject": "Java",
  "difficulty": "medium",
  "questionCount": 10,
  "questionTypes": ["single"],
  "mode": "STANDARD",
  "content": "可选：教材文本（PDF 已由主系统解析）"
}
```

`mode`：`FAST` 只生成不评估；`STANDARD` 生成 + LLM-as-a-Judge 质量评估。

返回统一结构：`steps / qualifiedQuestions / rejectedQuestions / evaluationFailedQuestions / totalCount / qualifiedCount / rejectedCount / evaluationFailedCount / processingTime`。

## 429 限流处理

- 质量评估并发限制为 **2**（`Semaphore`）
- 429 重试指数退避 **2s → 5s → 10s**，优先遵循 `Retry-After` 响应头
- API 失败（429/网络错误）题目标记 `EVALUATION_FAILED` 进入 `evaluationFailedQuestions`，**不会**误判为质量不合格（`QUALITY_REJECTED`）

## 配置（环境变量）

| 变量 | 默认值 | 说明 |
| ---- | ---- | ---- |
| `DEEPSEEK_API_KEY` | （必填） | DeepSeek API Key |
| `DEEPSEEK_BASE_URL` | `https://api.deepseek.com` | OpenAI 兼容接口地址 |
| `DEEPSEEK_MODEL` | `deepseek-chat` | 模型名 |
| `AI_QUALITY_THRESHOLD` | `4.0` | 质量评估合格分数线 |
| `AI_EVALUATION_CONCURRENCY` | `2` | 评估并发数 |

## 本地启动

```bash
# JDK 17
set DEEPSEEK_API_KEY=your-key
mvn spring-boot:run        # 默认端口 8081
```

## Docker

```bash
# 项目根目录
docker compose up -d ai-service
curl http://localhost:8081/actuator/health
```

## 版本兼容性说明

Spring AI 官方 `spring-ai-starter-model-deepseek` 自 1.0.0 GA 后才提供，且 2.1.0-M1 依赖 Spring Framework 6.2，与 Spring Boot 3.2.5（Spring 6.1）不兼容。因此本服务使用 **Spring AI 1.0.0-M6 的 OpenAI Starter**，通过 DeepSeek 的 OpenAI 兼容接口调用模型——调用链完整经过 Spring AI `ChatClient`，后续升级 Spring AI 版本后可平滑切换到 DeepSeek 专用 Starter。
