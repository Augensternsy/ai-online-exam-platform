@echo off
echo =========================================
echo 学之思考试系统 - 防作弊自动化回归测试
echo =========================================

echo.
echo 步骤 1: 安装依赖...
pip install -r requirements.txt

echo.
echo 步骤 2: 生成测试视频...
python utils/video_generator.py

echo.
echo 步骤 3: 运行自动化测试...
pytest test_cases/ -v --html=reports/test_report_%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%.html --self-contained-html

echo.
echo 步骤 4: 清理环境...
echo 测试完成，浏览器已自动关闭

echo.
echo =========================================
echo 测试报告已生成: reports/test_report_*.html
echo =========================================
pause
