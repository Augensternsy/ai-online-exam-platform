@echo off
echo ========================================
echo JMeter 压测运行脚本
echo ========================================
echo.

REM 设置 JMeter 路径（请修改为实际路径）
set JMETER_HOME=E:\Program Files\apache-jmeter-5.6.3
set JMETER=%JMETER_HOME%\bin\jmeter.bat

REM 检查 JMeter 是否存在
if not exist "%JMETER%" (
    echo ❌ 未找到 JMeter，请设置正确的 JMETER_HOME 路径
    echo 当前路径：%JMETER%
    pause
    exit /b 1
)

echo 请选择要运行的压测场景：
echo.
echo 1. 数据预埋（5 万条基准数据）
echo 2. 正常交卷压测（50 并发）
echo 3. 集中交卷压测（500 并发）
echo 4. 极端并发压测（1000 并发）
echo 5. 阶梯加压模型（50→1000）
echo 6. 查看压测报告
echo.
set /p choice=请输入选项 (1-6): 

if "%choice%"=="1" (
    echo.
    echo 正在运行数据预埋脚本...
    python data_seeder.py
) else if "%choice%"=="2" (
    echo.
    echo 正在运行正常交卷压测（50 并发）...
    "%JMETER%" -n -t jmeter_test_plan.jmx -l reports/jmeter_50_report.jtl -e -o reports/jmeter_50_report -Jthread_count=50 -Jramp_time=10
) else if "%choice%"=="3" (
    echo.
    echo 正在运行集中交卷压测（500 并发）...
    "%JMETER%" -n -t jmeter_test_plan.jmx -l reports/jmeter_500_report.jtl -e -o reports/jmeter_500_report -Jthread_count=500 -Jramp_time=50
) else if "%choice%"=="4" (
    echo.
    echo 正在运行极端并发压测（1000 并发）...
    "%JMETER%" -n -t jmeter_test_plan.jmx -l reports/jmeter_1000_report.jtl -e -o reports/jmeter_1000_report -Jthread_count=1000 -Jramp_time=100
) else if "%choice%"=="5" (
    echo.
    echo 正在运行阶梯加压模型...
    echo 阶段 1: 50 并发，10 秒
    "%JMETER%" -n -t jmeter_test_plan.jmx -l reports/jmeter_step_report.jtl -Jthread_count=50 -Jramp_time=10
    timeout /t 10 /nobreak >nul
    
    echo 阶段 2: 100 并发，10 秒
    "%JMETER%" -n -t jmeter_test_plan.jmx -l reports/jmeter_step_report.jtl -Jthread_count=100 -Jramp_time=10
    timeout /t 10 /nobreak >nul
    
    echo 阶段 3: 200 并发，10 秒
    "%JMETER%" -n -t jmeter_test_plan.jmx -l reports/jmeter_step_report.jtl -Jthread_count=200 -Jramp_time=10
    timeout /t 10 /nobreak >nul
    
    echo 阶段 4: 500 并发，10 秒
    "%JMETER%" -n -t jmeter_test_plan.jmx -l reports/jmeter_step_report.jtl -Jthread_count=500 -Jramp_time=20
    timeout /t 10 /nobreak >nul
    
    echo 阶段 5: 1000 并发，10 秒
    "%JMETER%" -n -t jmeter_test_plan.jmx -l reports/jmeter_step_report.jtl -Jthread_count=1000 -Jramp_time=50
    
    echo.
    echo 正在生成 HTML 报告...
    "%JMETER%" -g reports/jmeter_step_report.jtl -o reports/jmeter_step_report
) else if "%choice%"=="6" (
    echo.
    echo 正在打开压测报告...
    if exist "reports\jmeter_report\index.html" (
        start reports\jmeter_report\index.html
    ) else (
        echo ❌ 未找到报告文件，请先运行压测
    )
) else (
    echo.
    echo 无效的选项！
    pause
    exit /b 1
)

echo.
echo ========================================
echo 压测完成！
echo ========================================
pause
