# 学之思考试系统 - 防作弊自动化测试

## 项目简介
基于 Python + Selenium + Pytest 的自动化测试框架，用于测试考试系统的防作弊功能。

## 功能特性
- 虚拟视频流注入（无人/多人场景模拟）
- 切屏事件 JS 注入
- 核心业务流程自动化
- 多维度防作弊测试
- 自动化回归测试
- HTML 测试报告生成

## 环境要求
- Python 3.8+
- Chrome 浏览器
- ChromeDriver

## 安装依赖
```bash
pip install -r requirements.txt
```

## 运行测试
```bash
# 运行所有测试
pytest tests/ -v --html-report=reports/

# 运行特定测试
pytest tests/test_no_face.py -v

# 参数化测试
pytest tests/test_switch_screen.py -v --param="1,3,5"
```

## 测试用例
| 用例 | 描述 |
|------|------|
| test_no_face | 测试无人场景检测 |
| test_multiple_faces | 测试多人场景检测 |
| test_switch_screen | 测试切屏检测 |
| test_copy_paste | 测试复制粘贴检测 |
| test_camera_permission_denied | 测试摄像头权限拒绝 |
| test_camera_occupied | 测试摄像头被占用 |

## 目录结构
```
tests/
├── conftest.py           # Pytest 配置
├── base/                 # 基础类
│   └── base_test.py     # 测试基类
├── pages/                # 页面对象
│   ├── login_page.py    # 登录页
│   └── exam_page.py     # 考试页
├── test_cases/           # 测试用例
│   ├── test_no_face.py
│   ├── test_multiple_faces.py
│   ├── test_switch_screen.py
│   └── test_boundary.py
├── utils/                # 工具类
│   ├── video_generator.py  # 视频生成
│   └── assertions.py      # 断言工具
└── reports/              # 测试报告
```
