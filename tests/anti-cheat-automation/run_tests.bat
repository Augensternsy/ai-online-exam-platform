@echo off
echo ========================================
echo 防作弊自动化测试 - Windows 执行脚本
echo ========================================

cd /d "%~dp0"

echo [1/4] 检查 Python 环境...
python --version
if %errorlevel% neq 0 (
    echo 错误: 未找到 Python，请先安装 Python 3.8+
    pause
    exit /b 1
)

echo [2/4] 安装依赖...
pip install -r requirements.txt -q
if %errorlevel% neq 0 (
    echo 错误: 依赖安装失败
    pause
    exit /b 1
)

echo [3/4] 生成测试视频...
python generate_test_videos.py
if %errorlevel% neq 0 (
    echo 警告: 测试视频生成失败，继续执行测试
)

echo [4/4] 执行防作弊测试...
mkdir reports 2>nul
mkdir allure-results 2>nul

pytest test_anti_cheat.py ^
    -v ^
    --tb=short ^
    --html=reports/test_report.html ^
    --self-contained-html ^
    --alluredir=allure-results

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo 测试全部通过！
    echo 报告位置: reports/test_report.html
    echo ========================================
) else (
    echo.
    echo ========================================
    echo 测试失败！请查看报告详情
    echo 报告位置: reports/test_report.html
    echo ========================================
)

echo.
echo 查看 Allure 报告: allure serve allure-results
pause
