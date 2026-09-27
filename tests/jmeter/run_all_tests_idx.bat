@echo off
chcp 65001 >nul
set JMETER="E:\Program Files\apache-jmeter-5.6.3\bin\jmeter.bat"
set TEST_PLAN=jmeter_test_plan.jmx
set REPORT_DIR=reports

echo ========================================
echo 阶梯加压测试 - 有索引版本
echo ========================================
echo.

set THREADS=50 100 200 500 1000 2000 5000
set RAMP=10 20 40 50 50 50 100

echo 开始有索引测试...
echo.

for /L %%i in (0,1,6) do (
    call :run_test %%i
)

echo.
echo ========================================
echo 有索引测试完成！
echo ========================================
goto :end

:run_test
setlocal
set idx=%1
for /f "tokens=%idx% delims= " %%a in ("%THREADS%") do set t=%%a
for /f "tokens=%idx% delims= " %%a in ("%RAMP%") do set r=%%a

echo [有索引] 并发=%t%, 加压时间=%r%秒
del "%REPORT_DIR%\jmeter_idx_%t%.jtl" 2>nul
%JMETER% -n -t %TEST_PLAN% -l "%REPORT_DIR%\jmeter_idx_%t%.jtl" -Jthread_count=%t% -Jramp_time=%r%
echo.
endlocal
goto :eof

:end
pause
