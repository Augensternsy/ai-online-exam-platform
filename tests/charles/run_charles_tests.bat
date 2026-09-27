@echo off
echo ========================================
echo Charles 自动化测试运行脚本
echo ========================================
echo.

REM 设置 Python 路径
set PYTHON=C:\Users\pc\AppData\Local\Programs\Python\Python313\python.exe

echo 请选择要运行的测试类型：
echo.
echo 1. 弱网模拟测试
echo 2. 异常状态码注入测试
echo 3. Map Local/Remote 测试
echo 4. 请求抓包分析测试
echo 5. 运行所有 Charles 测试
echo.
set /p choice=请输入选项 (1-5): 

if "%choice%"=="1" (
    echo.
    echo 正在运行弱网模拟测试...
    %PYTHON% -m pytest charles/test_weak_network.py -v --html=reports/charles_weak_network_report.html
) else if "%choice%"=="2" (
    echo.
    echo 正在运行异常状态码注入测试...
    %PYTHON% -m pytest charles/test_error_injection.py -v --html=reports/charles_error_injection_report.html
) else if "%choice%"=="3" (
    echo.
    echo 正在运行 Map Local/Remote 测试...
    %PYTHON% -m pytest charles/test_mock_and_map.py -v --html=reports/charles_mock_map_report.html
) else if "%choice%"=="4" (
    echo.
    echo 正在运行请求抓包分析测试...
    %PYTHON% -m pytest charles/test_packet_analysis.py -v --html=reports/charles_packet_analysis_report.html
) else if "%choice%"=="5" (
    echo.
    echo 正在运行所有 Charles 测试...
    %PYTHON% -m pytest charles/ -v --html=reports/charles_all_tests_report.html
) else (
    echo.
    echo 无效的选项！
    pause
    exit /b 1
)

echo.
echo ========================================
echo 测试完成！
echo 测试报告已生成到 reports/ 目录
echo ========================================
pause
