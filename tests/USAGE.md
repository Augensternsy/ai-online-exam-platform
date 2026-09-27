# 学之思考试系统 - 防作弊自动化测试使用指南

## 一、环境准备

### 1.1 安装 Python
确保已安装 Python 3.8+：
```bash
python --version
```

### 1.2 安装依赖
```bash
cd tests
pip install -r requirements.txt
```

### 1.3 安装 Chrome 浏览器
确保已安装最新版本的 Google Chrome 浏览器。

## 二、项目结构

```
tests/
├── conftest.py              # Pytest 配置和 fixtures
├── pytest.ini               # Pytest 配置文件
├── requirements.txt         # Python 依赖
├── run_tests.bat            # Windows 运行脚本
├── run_tests.sh             # Linux/Mac 运行脚本
├── base/
│   └── base_page.py        # 页面对象基类
├── pages/
│   ├── login_page.py       # 登录页面对象
│   └── exam_page.py        # 考试页面对象
├── test_cases/
│   ├── test_no_face.py     # 无人场景测试
│   ├── test_multiple_faces.py  # 多人场景测试
│   ├── test_switch_screen.py   # 切屏检测测试
│   ├── test_boundary.py    # 边界条件测试
│   └── test_full_workflow.py   # 完整工作流测试
├── utils/
│   ├── video_generator.py  # 测试视频生成工具
│   └── assertions.py       # 断言工具
├── test_videos/            # 测试视频存储目录
└── reports/                # 测试报告存储目录
```

## 三、核心功能实现

### 3.1 虚拟视频流注入

**原理**：通过 Chrome 启动参数，将本地视频文件伪装成摄像头实时画面。

**实现代码**：
```python
chrome_options.add_argument('--use-fake-ui-for-media-stream')
chrome_options.add_argument('--use-fake-device-for-media-stream')
chrome_options.add_argument('--use-file-for-fake-video-capture=no_face.y4m')
```

**测试场景**：
- `no_face.y4m`：空座位视频，触发"考生离开"检测
- `multiple_faces.y4m`：多人视频，触发"他人入镜"检测
- `single_face.y4m`：单人视频，正常考试场景

### 3.2 切屏事件 JS 注入

**原理**：通过 Selenium 的 `execute_script()` 方法，直接触发浏览器的 `visibilitychange` 事件。

**实现代码**：
```python
def trigger_page_hidden(self):
    self.execute_script("""
        Object.defineProperty(document, 'hidden', {value: true, writable: true});
        document.dispatchEvent(new Event('visibilitychange'));
    """)
```

**测试场景**：
- 切屏 1 次：警告提示
- 切屏 3 次：警告提示
- 切屏 5 次：自动交卷

### 3.3 核心业务流程模拟

**流程**：
1. 启动浏览器 → 2. 登录系统 → 3. 进入考试 → 4. 触发异常 → 5. 验证结果 → 6. 清理环境

**实现代码**：
```python
def test_switch_screen_detection(self, driver, switch_times):
    # 1. 登录
    login_page.login('testuser', 'testpassword')
    
    # 2. 进入考试
    exam_page.start_exam(1)
    
    # 3. 触发切屏
    for i in range(switch_times):
        exam_page.trigger_page_hidden()
        exam_page.trigger_page_visible()
    
    # 4. 验证结果
    Assertions.assert_violation_warning_shown(exam_page)
```

## 四、运行测试

### 4.1 运行所有测试
```bash
cd tests
pytest test_cases/ -v
```

### 4.2 运行特定测试
```bash
# 无人场景测试
pytest test_cases/test_no_face.py -v

# 切屏检测测试
pytest test_cases/test_switch_screen.py -v

# 边界条件测试
pytest test_cases/test_boundary.py -v
```

### 4.3 参数化测试
```bash
# 测试切屏 1、3、5 次的不同情况
pytest test_cases/test_switch_screen.py::TestSwitchScreen::test_switch_screen_detection -v
```

### 4.4 生成测试报告
```bash
# HTML 报告
pytest test_cases/ -v --html=reports/test_report.html --self-contained-html

# Allure 报告
pytest test_cases/ -v --alluredir=allure-results
allure serve allure-results
```

### 4.5 使用运行脚本
```bash
# Windows
run_tests.bat

# Linux/Mac
chmod +x run_tests.sh
./run_tests.sh
```

## 五、测试用例说明

| 测试文件 | 测试用例 | 描述 | 预期结果 |
|---------|---------|------|---------|
| test_no_face.py | test_no_face_detection | 无人场景检测 | 显示违规警告 |
| test_no_face.py | test_no_face_auto_submit | 无人场景自动交卷 | 考试自动结束 |
| test_multiple_faces.py | test_multiple_faces_detection | 多人场景检测 | 显示违规警告 |
| test_multiple_faces.py | test_multiple_faces_auto_submit | 多人场景自动交卷 | 考试自动结束 |
| test_switch_screen.py | test_switch_screen_detection | 切屏检测（参数化） | 1/3 次警告，5 次交卷 |
| test_switch_screen.py | test_switch_screen_boundary | 切屏边界测试 | 第 5 次自动交卷 |
| test_switch_screen.py | test_rapid_switch_screen | 快速切屏测试 | 检测并交卷 |
| test_boundary.py | test_copy_paste_detection | 复制粘贴检测 | 显示禁止提示 |
| test_boundary.py | test_camera_permission_denied | 摄像头权限拒绝 | 显示权限对话框 |
| test_boundary.py | test_exam_timeout | 考试超时测试 | 自动交卷 |
| test_boundary.py | test_network_disconnect | 网络断开测试 | 正常处理 |

## 六、自动化回归测试

### 6.1 触发时机
- 后端接口更新后
- 前端防作弊算法修改后
- 代码提交触发构建时

### 6.2 执行命令
```bash
# 一键运行所有回归测试
pytest test_cases/ -v --html=reports/regression_report.html

# 并行执行（加快速度）
pytest test_cases/ -v -n 4
```

### 6.3 结果判断
- **全绿（PASS）**：代码更新未破坏防作弊逻辑
- **红色（FAIL）**：存在 Bug，需要修复

## 七、常见问题

### 7.1 ChromeDriver 版本不匹配
```bash
pip install webdriver-manager
```

### 7.2 摄像头权限弹窗
确保在 `conftest.py` 中配置了 `--use-fake-ui-for-media-stream` 参数。

### 7.3 测试视频生成
```bash
python utils/video_generator.py
```

### 7.4 测试超时
增加 `pytest.ini` 中的超时时间：
```ini
addopts = -v --timeout=60
```

## 八、扩展开发

### 8.1 添加新测试用例
在 `test_cases/` 目录下创建新的测试文件：
```python
import pytest
from pages.login_page import LoginPage
from pages.exam_page import ExamPage

class TestNewFeature:
    def test_new_feature(self, driver):
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')
        
        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        
        # 添加你的测试逻辑
        assert True
```

### 8.2 添加新页面对象
在 `pages/` 目录下创建新的页面对象类：
```python
from base.base_page import BasePage
from selenium.webdriver.common.by import By

class NewPage(BasePage):
    NEW_ELEMENT = (By.CSS_SELECTOR, ".new-element")
    
    def click_new_element(self):
        self.click_element(*self.NEW_ELEMENT)
```

## 九、CI/CD 集成

### 9.1 GitHub Actions
```yaml
name: Anti-Cheat Tests
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Setup Python
        uses: actions/setup-python@v2
        with:
          python-version: '3.8'
      - name: Install dependencies
        run: |
          cd tests
          pip install -r requirements.txt
      - name: Run tests
        run: |
          cd tests
          pytest test_cases/ -v --html=reports/report.html
      - name: Upload report
        uses: actions/upload-artifact@v2
        with:
          name: test-report
          path: tests/reports/
```

### 9.2 Jenkins
```groovy
pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                dir('tests') {
                    sh 'pip install -r requirements.txt'
                    sh 'pytest test_cases/ -v --html=reports/report.html'
                }
            }
        }
    }
}
```

## 十、最佳实践

1. **保持测试独立性**：每个测试用例应该可以独立运行
2. **使用 Page Object 模式**：提高代码可维护性
3. **添加适当等待**：避免使用 `time.sleep()`，优先使用显式等待
4. **参数化测试**：减少重复代码
5. **定期清理环境**：确保测试后清理浏览器和缓存
6. **生成详细报告**：便于问题定位和追踪
