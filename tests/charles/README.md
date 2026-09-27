# Charles 测试配置指南

## 一、Charles 安装与配置

### 1.1 下载与安装
- 官网：https://www.charlesproxy.com/
- 下载 Windows 版本并安装

### 1.2 SSL 证书配置
1. 打开 Charles → Help → SSL Proxying → Install Charles Root Certificate
2. 安装证书到"受信任的根证书颁发机构"
3. 在 Charles 中启用 SSL Proxying：
   - Proxy → SSL Proxying Settings
   - 勾选 Enable SSL Proxying
   - 添加 Host: `*` Port: `443`

### 1.3 浏览器代理配置
- Charles 默认代理：`127.0.0.1:8888`
- Chrome 启动参数：
  ```bash
  chrome.exe --proxy-server=127.0.0.1:8888
  ```

## 二、弱网模拟配置

### 2.1 开启 Throttle 功能
1. Proxy → Throttle Settings
2. 勾选 Enable Throttling
3. 配置参数：

| 场景 | Bandwidth | Latency | Reliability |
|------|-----------|---------|-------------|
| 3G 网络 | 50 Kbps | 300ms | 90% |
| 高铁网络 | 20 Kbps | 500ms | 80% |
| 校园网卡 | 100 Kbps | 200ms | 85% |
| 极端弱网 | 10 Kbps | 1000ms | 70% |

### 2.2 验证点
- 视频流监控是否误报"考生离开"
- 前端能否区分网络卡顿和真实违纪
- 超时重试机制是否正常工作

## 三、异常状态码注入

### 3.1 使用 Breakpoints（断点法）
1. 右键点击目标接口 → Breakpoints
2. 当请求返回时，Charles 会暂停
3. 手动修改 HTTP Status：
   - 200 → 500（服务器错误）
   - 200 → 502（网关错误）
   - 200 → 503（服务不可用）
4. 点击 Execute 放行

### 3.2 使用 Rewrite（自动重写）
1. Tools → Rewrite
2. 勾选 Enable Rewrite
3. 添加规则：

**规则 1：交卷接口 500 错误**
```
Location: Host
Match: localhost:8080
Where: Response Status
Match: 200
Replace: 500
```

**规则 2：监控接口超时**
```
Location: Path
Match: /api/exam/monitor
Where: Response Status
Match: 200
Replace: 504
```

## 四、Map Local（本地数据 Mock）

### 4.1 配置步骤
1. 右键点击目标请求 → Map Local
2. 选择本地 JSON 文件
3. 前端请求将被拦截并返回本地数据

### 4.2 Mock 数据示例
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "examId": 1,
    "examName": "模拟考试",
    "questions": [...]
  }
}
```

## 五、Map Remote（环境转发）

### 5.1 配置步骤
1. Tools → Map Remote
2. 勾选 Enable Map Remote
3. 添加映射规则：

| 源地址 | 目标地址 | 用途 |
|--------|---------|------|
| localhost:8080 | test.example.com | 本地→测试环境 |
| localhost:8080 | prod.example.com | 本地→生产环境 |

## 六、请求抓包与报文分析

### 6.1 抓包步骤
1. 启动 Charles
2. 浏览器访问考试系统
3. 在 Charles 中查看请求列表

### 6.2 分析要点
- Request Headers：Token 是否正确
- Request Payload：坐标格式是否正确
- Response Status：后端返回状态码
- Response Body：数据格式是否正确

### 6.3 责任界定流程
```
前端是否发起请求？
├─ 否 → 前端问题
└─ 是 → 检查 Request Payload
         ├─ 格式错误 → 前端问题
         └─ 格式正确 → 检查 Response
                      ├─ 状态码错误 → 后端问题
                      └─ 数据错误 → 后端问题
```

## 七、Charles 性能优化

### 7.1 JVM 内存配置
编辑 `charles.vmoptions`：
```
-Xms512m
-Xmx2048m
```

### 7.2 过滤无关请求
1. Proxy → Recording Settings
2. 添加 Include/Exclude 规则
3. 只记录目标域名的请求

## 八、Charles vs Fiddler 对比

| 特性 | Charles | Fiddler |
|------|---------|---------|
| UI 界面 | 树状视图，清晰 | 瀑布流，信息密集 |
| Mock 配置 | 可视化，秒级生效 | 需编写 FiddlerScript |
| 动态编程 | 基于正则，灵活性低 | 支持 C# 脚本，灵活性高 |
| 内存占用 | Java 开发，占用较高 | .NET 开发，占用较低 |
| 跨平台 | 支持 Windows/Mac/Linux | 仅支持 Windows |

## 九、自动化测试集成

### 9.1 与 Selenium 集成
```python
from selenium import webdriver
from selenium.webdriver.chrome.options import Options

chrome_options = Options()
chrome_options.add_argument('--proxy-server=127.0.0.1:8888')
driver = webdriver.Chrome(options=chrome_options)
```

### 9.2 与 Pytest 集成
```python
@pytest.fixture
def weak_network_driver():
    """弱网环境下的 WebDriver"""
    # 先配置 Charles 弱网规则
    configure_charles_throttle()
    
    chrome_options = Options()
    chrome_options.add_argument('--proxy-server=127.0.0.1:8888')
    driver = webdriver.Chrome(options=chrome_options)
    
    yield driver
    
    driver.quit()
    # 恢复 Charles 正常配置
    reset_charles_throttle()
```

## 十、测试用例清单

| 测试场景 | Charles 配置 | 验证点 |
|---------|-------------|--------|
| 3G 网络环境 | Throttle: 50Kbps, 300ms | 视频流不误报 |
| 高铁网络环境 | Throttle: 20Kbps, 500ms | 超时重试正常 |
| 后端 500 错误 | Rewrite: 200→500 | 前端异常捕获 |
| 后端 502 错误 | Rewrite: 200→502 | 错误提示友好 |
| 交卷接口超时 | Breakpoints: 手动延迟 | 本地缓存兜底 |
| 监控接口失败 | Rewrite: 200→504 | 不影响考试 |
