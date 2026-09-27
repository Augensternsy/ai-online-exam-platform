# JMeter 压测完整实现指南

## 📋 JMeter 简介

JMeter 是由 Apache 开源的一款纯 Java 编写的压测工具，主要用于对软件做性能测试、负载测试和压力测试。

### 核心作用
- **模拟高并发**：瞬间变出几百、几千个"虚拟用户"
- **压力测试**：在同一秒钟整齐划一地疯狂请求某个接口
- **性能调优**：发现系统瓶颈（如全表扫描问题）

### 考试场景痛点
> 考试结束前的一分钟，全校上千名考生会同时狂点交卷按钮。如果不加压测直接上线，一旦数据库扛不住瞬间锁死，前端就会集体卡顿甚至白屏。

## 🎯 JMeter vs Postman

| 维度 | Postman (功能/接口测试) | JMeter (性能/压力测试) |
|------|------------------------|------------------------|
| 核心定位 | "点对点"的精密手术刀 | "集群式"的机关枪 |
| 执行方式 | 主要是串行执行 | 天然支持并行执行 |
| 性能消耗 | 较重，界面华丽 | 较轻，支持 CLI 模式 |
| 报告侧重 | Assert 结果 | TPS、RT、错误率 |

## 🚀 项目实施步骤

### 第一步：数据预埋（5 万条基准数据）

#### 数据构成
- 上万个真实的考生账号
- 海量的题库与试卷信息
- 上万条历史和进行中的考试记录

#### 生成方式
- **Faker 库**：生成伪数据（中文姓名、学号、时间戳）
- **内存批处理**：每 1000 条为一批次
- **executemany**：批量插入，减少网络往返

### 第二步：核心业务链路配置

#### HTTP 采样器配置
- URL：交卷接口地址
- 请求头：Content-Type、Authorization
- JSON 参数：携带动态 Token

### 第三步：阶梯加压模型

#### 加压策略
```
50 线程 → 10 秒 → 稳定
+50 线程 → 10 秒 → 稳定
+50 线程 → 10 秒 → 稳定
...
```

#### 优势
- 渐进式施压
- 精准探测性能拐点
- 避免直接压垮系统

### 第四步：性能调优

#### 问题定位
1. 盯着聚合报告
2. TPS 突然上不去
3. RT 从几十毫秒飙升到几秒

#### 数据库调优
1. 查看 MySQL 慢查询日志
2. 使用 EXPLAIN 分析执行计划
3. 添加联合索引

## 📊 核心指标

| 指标 | 说明 | 正常值 |
|------|------|--------|
| TPS | 每秒处理事务数 | 越高越好 |
| RT | 平均响应时间 | < 1 秒 |
| 错误率 | 失败请求占比 | < 1% |
| 并发数 | 同时在线用户数 | 根据场景 |

## 🔧 高并发解决方案

### 方案一：消息队列（MQ）削峰填谷
- 同步写库 → 异步处理
- MQ 先扛下并发
- 后端消费者以健康速率写入数据库

### 方案二：Redis 内存缓存
- 交卷数据先写入 Redis
- 考试结束后批量同步回 MySQL
- "先写内存，后落磁盘"

### 方案三：前端打散流量
- 交卷请求加 0-5 秒随机延迟
- 将 1 毫秒内的 1 万并发打散到 5 秒
- 工程上称为"请求抖动（Jitter）"

## 🔍 "转圈圈"问题排查

### 第一步：前端与网络层
- F12 开发者工具
- Charles 抓包
- 查看请求状态（Pending/Failed）
- 检查 Payload 报文大小

### 第二步：应用服务层
- 查 Tomcat 线程池（默认 200 个）
- 查数据库连接池（HikariCP）
- jstack 命令打线程快照

### 第三步：数据库层
- SHOW PROCESSLIST;
- 检查表锁/死锁
- 查看慢查询日志

### 第四步：兜底方案
- 设置合理 Timeout（15 秒）
- 弹窗提示"当前交卷人数过多"
- 接口幂等性设计

## 📈 数据库优化

### 慢查询日志
```sql
-- 开启慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;
```

### EXPLAIN 分析
```sql
EXPLAIN UPDATE exam_record SET status = 'submitted' 
WHERE exam_id = ? AND user_id = ?;
```

**关键指标：**
- type: ALL（全表扫描）→ 需要优化
- possible_keys: NULL（无索引）→ 需要添加

### 联合索引
```sql
-- 添加联合索引
ALTER TABLE exam_record ADD INDEX idx_exam_user (exam_id, user_id);
```

**最左前缀法则：**
- 数据先按 exam_id 排序
- 相同 exam_id 再按 user_id 排序
- 比两个单列索引效率更高

## 📁 项目文件结构

```
tests/jmeter/
├── README.md                      # 使用指南
├── data_seeder.py                 # 数据预埋脚本
├── jmeter_test_plan.jmx           # JMeter 测试计划
├── run_jmeter.bat                 # Windows 运行脚本
├── run_jmeter.sh                  # Linux/Mac 运行脚本
├── mock_data/                     # Mock 数据
│   ├── students.csv               # 学生数据
│   ├── exams.csv                  # 考试数据
│   └── exam_records.csv           # 考试记录
└── reports/                       # 压测报告
    ├── jmeter_report.html         # HTML 报告
    └── jmeter_report.jtl          # 原始数据
```

## 🎯 测试覆盖场景

| 场景 | 并发数 | 验证点 |
|------|--------|--------|
| 正常交卷 | 50 | 基础功能正常 |
| 集中交卷 | 500 | TPS 稳定 |
| 极端并发 | 1000 | 系统不崩溃 |
| 阶梯加压 | 50→1000 | 性能拐点 |

## ⚡ 快速开始

### 1. 安装 JMeter
```bash
# 下载 JMeter
https://jmeter.apache.org/download_jmeter.cgi

# 解压并配置环境变量
```

### 2. 预埋数据
```bash
python data_seeder.py
```

### 3. 运行压测
```bash
# Windows
run_jmeter.bat

# Linux/Mac
chmod +x run_jmeter.sh
./run_jmeter.sh
```

### 4. 查看报告
打开 `reports/jmeter_report.html`

## 📝 注意事项

1. **JMeter 必须先安装** - 需要 Java 环境
2. **数据库连接配置** - 确保 MySQL 可访问
3. **数据量足够** - 5 万条基准数据
4. **监控服务器** - 压测时观察 CPU、内存、磁盘

## 🎉 预期收益

1. ✅ 发现全表扫描问题
2. ✅ 优化数据库索引
3. ✅ 验证系统承载能力
4. ✅ 保障集中交卷不卡顿
5. ✅ 提升系统鲁棒性
