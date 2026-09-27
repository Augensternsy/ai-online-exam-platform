# Charles 测试使用指南

## 📋 测试概述

Charles 测试需要配合 Charles 软件使用，以下是完整的配置和测试流程。

## 🚀 前置准备

### 1. 安装 Charles
- 官网下载：https://www.charlesproxy.com/
- 安装并启动 Charles

### 2. 配置 SSL 证书
1. 打开 Charles → Help → SSL Proxying → Install Charles Root Certificate
2. 安装证书到"受信任的根证书颁发机构"
3. 在 Charles 中启用 SSL Proxying：
   - Proxy → SSL Proxying Settings
   - 勾选 Enable SSL Proxying
   - 添加 Host: `*` Port: `443`

### 3. 配置浏览器代理
- Charles 默认代理：`127.0.0.1:8888`
- Chrome 启动参数：`--proxy-server=127.0.0.1:8888`

## 📝 测试场景

### 场景 1：弱网模拟测试

#### Charles 配置
1. Proxy → Throttle Settings
2. 勾选 Enable Throttling
3. 选择预设或自定义：

| 场景 | Bandwidth | Latency | Reliability |
|------|-----------|---------|-------------|
| 3G 网络 | 50 Kbps | 300ms | 90% |
| 高铁网络 | 20 Kbps | 500ms | 80% |
| 极端弱网 | 10 Kbps | 1000ms | 70% |

#### 运行测试
```bash
C:\Users\pc\AppData\Local\Programs\Python\Python313\python.exe -m pytest charles/test_weak_network.py -v
```

#### 验证点
- ✅ 视频流监控是否误报"考生离开"
- ✅ 前端能否区分网络卡顿和真实违纪
- ✅ 超时重试机制是否正常工作

---

### 场景 2：异常状态码注入

#### Charles 配置（Rewrite 方式）
1. Tools → Rewrite
2. 勾选 Enable Rewrite
3. 添加规则：

**规则 1：交卷接口 500 错误**
- Location: Host
- Match: localhost:8080
- Where: Response Status
- Match: 200
- Replace: 500

**规则 2：监控接口 504 超时**
- Location: Path
- Match: /api/exam/monitor
- Where: Response Status
- Match: 200
- Replace: 504

#### 运行测试
```bash
C:\Users\pc\AppData\Local\Programs\Python\Python313\python.exe -m pytest charles/test_error_injection.py -v
```

#### 验证点
- ✅ 500 错误：前端异常捕获
- ✅ 502 错误：错误提示友好
- ✅ 504 超时：不影响考试
- ✅ 重试机制：正常工作
- ✅ 本地缓存：兜底策略激活

---

### 场景 3：Map Local（本地数据 Mock）

#### Charles 配置
1. 准备 Mock 数据文件：
   - `mock_data/exam_paper.json` - 试卷数据
   - `mock_data/ai_analysis.json` - AI 分析结果
   - `mock_data/health_check.json` - 健康检查

2. 配置 Map Local：
   - 右键点击目标请求 → Map Local
   - 选择本地 JSON 文件
   - 前端请求将被拦截并返回本地数据

#### 运行测试
```bash
C:\Users\pc\AppData\Local\Programs\Python\Python313\python.exe -m pytest charles/test_mock_and_map.py -v
```

#### 验证点
- ✅ 试卷数据加载成功
- ✅ AI 接口响应正常
- ✅ 前端不依赖后端开发

---

### 场景 4：Map Remote（环境转发）

#### Charles 配置
1. Tools → Map Remote
2. 勾选 Enable Map Remote
3. 添加映射规则：

| 源地址 | 目标地址 | 用途 |
|--------|---------|------|
| localhost:8080 | test.example.com | 本地→测试环境 |
| localhost:8080 | prod.example.com | 本地→生产环境 |

#### 验证点
- ✅ 请求无缝转发
- ✅ 环境切换方便
- ✅ 前端无需修改代码

---

### 场景 5：请求抓包与报文分析

#### 使用步骤
1. 启动 Charles
2. 运行测试脚本
3. 在 Charles 中查看请求列表

#### 分析要点
- Request Headers：Token 是否正确
- Request Payload：坐标格式是否正确
- Response Status：后端返回状态码
- Response Body：数据格式是否正确

#### 责任界定流程
```
前端是否发起请求？
├─ 否 → 前端问题
└─ 是 → 检查 Request Payload
         ├─ 格式错误 → 前端问题
         └─ 格式正确 → 检查 Response
                      ├─ 状态码错误 → 后端问题
                      └─ 数据错误 → 后端问题
```

## 📊 测试报告

所有测试报告生成在 `reports/` 目录：
- `charles_weak_network_report.html` - 弱网模拟测试报告
- `charles_error_injection_report.html` - 异常注入测试报告
- `charles_mock_map_report.html` - Mock 映射测试报告
- `charles_packet_analysis_report.html` - 报文分析测试报告

## 🔧 快速运行

### Windows
```bash
cd tests
charles\run_charles_tests.bat
```

### Linux/Mac
```bash
cd tests
chmod +x charles/run_charles_tests.sh
./charles/run_charles_tests.sh
```

## ⚠️ 注意事项

1. **Charles 必须先启动** - 测试脚本依赖 Charles 代理
2. **SSL 证书必须安装** - 否则 HTTPS 请求会失败
3. **代理配置正确** - 浏览器必须使用 127.0.0.1:8888
4. **Mock 数据路径** - 确保 mock_data 目录下的 JSON 文件存在

## 📚 相关文件

- `test_weak_network.py` - 弱网模拟测试
- `test_error_injection.py` - 异常状态码注入测试
- `test_mock_and_map.py` - Map Local/Remote 测试
- `test_packet_analysis.py` - 请求抓包分析测试
- `mock_data/` - Mock 数据文件
- `rewrite_rules.json` - Charles Rewrite 规则配置
- `throttle_settings.xml` - Charles Throttle 配置

## 🎯 测试覆盖度

| 测试场景 | 测试用例数 | 状态 |
|---------|-----------|------|
| 弱网模拟 | 3 | ✅ 已实现 |
| 异常注入 | 5 | ✅ 已实现 |
| Map Local/Remote | 3 | ✅ 已实现 |
| 报文分析 | 3 | ✅ 已实现 |
| **总计** | **14** | **✅ 100%** |
