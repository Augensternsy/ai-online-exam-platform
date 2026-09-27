# AI 智能在线考试平台

## 项目简介

AI 智能在线考试平台是一个基于 **Spring Boot + Vue** 的前后端分离在线考试系统，引入 **Spring AI + DeepSeek** 实现智能试题生成（结构化 Prompt 控制题目格式、LLM-as-a-Judge 质量评估），并结合 **Selenium 自动化测试、JMeter 性能测试、Charles 抓包分析和 Docker / Kubernetes 容器化部署**，完成从开发、测试到部署的完整工程实践。

平台提供管理端与学生端：管理员可管理试卷、题目、用户并使用 AI 出题；学生可在线答题、查看成绩与错题本。系统内置完整 Demo 数据，**拉取代码即可体验全部核心流程**。

## 技术栈

**后端**

- Spring Boot
- Spring Security
- MyBatis
- MySQL
- Undertow

**前端**

- Vue
- Vue Router / Vuex
- Element UI
- Axios

**AI**

- Exam Agent 轻量级架构（需求理解 → 任务规划 → LLM 执行 → 结果验证 → 结果输出）
- Spring AI（DeepSeek 兼容接口）
- DeepSeek（deepseek-v3）
- 自然语言需求解析（LLM 解析 + 关键词规则回退）
- 结构化 JSON 输出校验与自动修复
- LLM-as-a-Judge 质量评估
- AI 智能评测（成绩分析 / 薄弱知识点 / 学习建议）
- 内置 DemoQuestionBank 兜底题库，LLM 不可用时 Demo 不中断
- PDFBox（PDF 教材解析）

**测试**

- Python + Pytest
- Selenium（含人脸监考异常场景模拟）
- JMeter（高并发压力测试）
- Charles（弱网 / Mock / 抓包分析）

**部署**

- Docker
- Docker Compose
- Kubernetes（配置见 `k8s/` 目录）
- Nginx

## 核心功能

### 1. AI Agent 自然语言出题

- 一句话需求即可生成试卷，例如「生成一套 Java 基础，中等难度，20 道题的考试」
- **Exam Agent 五段式流程**：需求理解 → 任务规划 → LLM 执行 → 结果验证 → 结果输出
- 需求解析支持 LLM 智能解析 + 关键词规则回退双保险
- 结构化 Prompt 严格控制题目输出 JSON 格式（题型 / 选项 / 答案 / 解析 / 知识点）
- 输出校验：JSON 格式校验、字段完整性检查、答案与选项匹配校验
- 异常处理：LLM 超时自动重试 3 次，全部失败后加载内置 DemoQuestionBank 兜底
- 支持单选题、多选题、判断题，可配置题型比例
- 前端四步过程可视化展示（理解需求 → 生成 Prompt → 调用模型 → 验证输出）

### 2. AI 智能评测

- 交卷后一键生成 AI 成绩分析
- 输出：总分 / 正确率 / 各题型正确率 / 薄弱知识点 / 学习建议
- 基于答题数据逐题分析，LLM 不可用时回退静态统计分析

### 3. 在线考试流程

- 用户登录 / 注册
- 试卷中心按学科、试卷类型筛选
- 在线答题，支持自动保存与断网本地缓存
- 交卷后自动评分，查看答案解析
- 错题本自动归集

### 4. 自动化测试

- Selenium 端到端自动化测试（含 AI Agent 出题闭环）
- 人脸监考异常场景模拟（遮挡、离屏、多人人脸等）

### 5. 性能测试

- JMeter 高并发压力测试（登录 / AI 生成 / 提交考试）
- SQL 执行计划分析与索引优化

### 6. 容器化部署

- Docker 镜像多阶段构建
- Docker Compose 一键编排 MySQL + 后端 + 前端
- Kubernetes Deployment / Service / Ingress 部署验证

## 系统架构

```
                    ┌──────────────┐
                    │     用户      │
                    └──────┬───────┘
                           │ HTTP
                    ┌──────▼───────┐
                    │   Vue 前端    │  管理端 / 学生端 (Element UI)
                    └──────┬───────┘
                           │ /api
                    ┌──────▼───────┐
                    │ Spring Boot  │  Spring Security + MyBatis
                    │   Exam Agent │  需求理解/规划/执行/验证/输出
                    └──────┬───────┘
                           │ JDBC
                    ┌──────▼───────┐
                    │    MySQL     │
                    └──────────────┘

AI 模块：  Exam Agent  ──►  Spring AI  ──►  DeepSeek（deepseek-v3）
                │
                └──►  DemoQuestionBank（LLM 不可用时兜底）

质量保障：  Selenium（功能） / JMeter（性能） / Charles（网络）
```

## Exam Agent 流程

```
自然语言需求文本
       │
       ▼
┌──────────────────┐
│  RequirementParser │  LLM 解析 → 失败回退关键词规则
│  (需求理解模块)    │  输出 ExamRequirement(科目/难度/题量/题型)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  PromptPlanner    │  构造结构化 Prompt（题型比例/JSON 格式约束）
│  (任务规划模块)    │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  LLMExecutor      │  调用 DeepSeek，最多重试 3 次
│  (LLM 执行模块)    │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│  OutputValidator  │  JSON 校验 / 字段完整性 / 答案匹配
│  (结果验证模块)    │  失败 → 重试；3 次全失败 → DemoQuestionBank 兜底
└────────┬─────────┘
         │
         ▼
  结构化题目列表 → 前端展示 / 保存题库
```

## 数据库设计

核心表结构（完整 DDL 见 `EMS.sql`）：

| 表名 | 说明 |
| ---- | ---- |
| `user` | 用户表（管理员 / 学生，含角色、状态） |
| `subject` | 学科表 |
| `question` | 题目表（content JSON 存储题干/选项/答案/解析） |
| `exam_paper` | 试卷表（frame JSON 存储题目分组与顺序） |
| `exam_paper_answer` | 答卷表（学生提交记录、得分、批改状态） |
| `text_content` | 题目内容表（与 question 关联，content 字段存 JSON） |

题目 `content` JSON 结构：
```json
{
  "titleContent": "题干",
  "analyze": "答案解析",
  "questionItemObjects": [
    {"prefix": "A", "content": "选项内容", "score": 2, "itemUuid": "..."}
  ],
  "correct": "A"
}
```

## 快速开始（本地开发）

```bash
# 1. 创建数据库并导入数据脚本（自动创建表结构 + Demo 数据）
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS ems DEFAULT CHARACTER SET utf8mb4;"
mysql -u root -p ems < EMS.sql

# 2. 启动后端（默认 8000 端口）
cd source/xzs
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run

# 3. 启动前端
cd source/vue/xzs-admin
npm install && npm run serve    # 管理端 http://localhost:8002

cd source/vue/xzs-student
npm install && npm run serve    # 学生端 http://localhost:8001
```

## Online Demo

**Demo URL：** https://ai-exam-platform.vercel.app/

| 角色 | 账号 | 密码 |
| ---- | ---- | ---- |
| 管理员 | `admin` | `123456` |
| 学生 | `Natasha` | `123456` |

> 在线版本使用 **Showcase Demo Mode**，数据为前端内置 Mock，无需连接真实后端即可体验完整流程。
>
> 完整源码支持：Spring Boot、MySQL、DeepSeek、Selenium、JMeter、Docker、Kubernetes。

## Demo 使用说明

系统已内置完整演示数据，**登录即可体验**，无需手动创建：

| 角色 | 账号 | 密码 | 入口 |
| ---- | ---- | ---- | ---- |
| 管理员 | `admin` | `123456` | 管理端 |
| 学生 | `Natasha` | `123456` | 学生端 |

推荐演示路径：

1. **管理端**：登录 →「AI 智能出题」→ 在「AI Agent 自然语言出题」输入框输入需求（如「生成一套 Java 基础，中等难度，8 道题的考试」）→ 点击生成 → 查看四步过程展示与题目列表 → 保存到题库
2. **学生端**：登录 → 试卷中心选择「Java 基础能力测试」开始答题 → 提交 → 查看成绩与解析 → 点击「AI 智能评测」查看正确率、薄弱知识点与学习建议

内置演示试卷「**Java 基础能力测试**」共 **20 道题（14 单选 + 3 多选 + 3 判断）/ 100 分**，覆盖 Java 基础知识、Spring Boot 基础、MySQL 基础，每题均含正确答案与解析。

> 💡 当 DeepSeek API 不可达时，Exam Agent 会自动回退到内置 DemoQuestionBank 兜底题库，演示流程不中断。

## 部署方式

### Docker Compose（推荐）

一键启动 **MySQL + Spring Boot + Vue/Nginx** 三个服务：

```bash
# 1. 克隆项目
git clone <repository-url> && cd xzs-mysql-master

# 2. 配置环境变量
cp .env.example .env            # Windows: copy .env.example .env
# 编辑 .env，填写数据库密码与 DEEPSEEK_API_KEY

# 3. 启动全部服务
docker compose up -d

# 4. 访问
# 学生端：  http://<服务器IP>/student/
# 管理端：  http://<服务器IP>/admin/
```

包含服务：

| 服务 | 说明 | 默认端口 |
| ---- | ---- | ---- |
| `db` | MySQL 8.0，首次启动自动导入 EMS.sql（含 Demo 数据） | 3306 |
| `backend` | Spring Boot 后端，多阶段 Maven 构建 | 8000 |
| `frontend` | Vue 管理端 + 学生端构建产物，Nginx 托管并反向代理 /api | 80 |

### Kubernetes

K8s 部署清单位于 `k8s/` 目录，包含 MySQL（PVC）、后端、前端的 Deployment / Service 及 Ingress：

```bash
kubectl apply -f k8s/
```

## 项目结构

```
xzs-mysql-master/
├── EMS.sql                    # 数据库脚本（表结构 + 完整 Demo 数据）
├── docker-compose.yml         # Docker Compose 编排
├── Dockerfile                 # 后端镜像（Maven 多阶段构建）
├── .env.example               # 环境变量示例
├── k8s/                       # Kubernetes 部署清单
├── source/
│   ├── xzs/                   # Spring Boot 后端
│   └── vue/
│       ├── xzs-admin/         # 管理端（Vue + Element UI）
│       ├── xzs-student/       # 学生端（Vue + Element UI）
│       ├── Dockerfile         # 前端镜像（双端构建 + Nginx）
│       └── nginx.conf         # 前端 Nginx 配置
└── tests/                     # Selenium / JMeter / Charles 测试工程
```

## 公网部署说明

公网部署架构：**云服务器 + Docker Compose + Nginx + 公网 IP / 域名**。

部署前准备：

1. 一台云服务器（建议 2 核 4G 及以上），安装 Docker 与 Docker Compose
2. 配置安全组 / 防火墙开放 **80（HTTP）** 端口（443 视 HTTPS 而定）
3. 准备域名并将 A 记录解析到服务器公网 IP（无域名时可直接用 IP 访问）
4. 申请 **DeepSeek API Key**（不配置 Key 时，AI 出题可使用页面内置 Demo 示例）
5. 复制 `.env.example` 为 `.env` 并修改数据库密码等敏感配置 —— **所有敏感信息通过环境变量注入，不写入代码**

## 测试方案

| 类型 | 工具 | 覆盖范围 | 位置 |
| ---- | ---- | ---- | ---- |
| 端到端功能测试 | Selenium | 登录 → AI 出题 → 答题 → 交卷 → 查成绩 | `tests/ai-agent-e2e/` |
| 人脸监考异常测试 | Selenium + 视频注入 | 遮挡、离屏、多人人脸等 | `tests/anti-cheat-automation/` |
| 接口压力测试 | JMeter | 登录 / AI 生成试卷 / 提交考试 | `tests/ai-agent-e2e/ai_exam_api_test.jmx` |
| 网络异常测试 | Charles | 弱网 / Mock / 错误注入 | `tests/charles/` |
| 异常场景 | 接口直连 | AI 超时 / 空参数 / JSON 错误 / 不存在答卷 | `tests/ai-agent-e2e/README.md` |

运行 AI Agent E2E 测试：

```bash
cd tests/ai-agent-e2e
pip install -r requirements.txt
python test_ai_agent_e2e.py
```

## 未来优化方向

1. **真正接入 Spring AI**：当前 LLM 调用基于 Apache HttpClient 封装，可迁移到 Spring AI `ChatClient` 统一接口
2. **Agent 记忆与多轮对话**：支持上下文连续出题（如「再加 5 道难度更高的」）
3. **知识库增强 RAG**：上传教材/讲义后基于向量检索生成贴合知识点的题目
4. **AI 阅卷主观题**：简答题/填空题由 LLM 语义判分，支持部分得分
5. **流式输出**：题目逐条流式返回，前端实时渲染，降低等待感知
6. **监控与链路追踪**：集成 Spring Boot Actuator + Prometheus + SkyWalking
7. **CI/CD**：GitHub Actions 自动构建镜像并推送，接入 SonarQube 代码质量门禁

## 许可证

本项目基于开源许可证发布，详见 [LICENSE](./LICENSE)。
