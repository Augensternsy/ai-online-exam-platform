import pytest
import time
import os
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from pages.login_page import LoginPage
from pages.exam_page import ExamPage
from utils.assertions import Assertions
from utils.video_generator import VideoGenerator

class TestExamAntiCheat:
    @pytest.fixture(autouse=True)
    def setup(self, driver):
        self.driver = driver
        self.exam_page = None
        yield
        if self.exam_page:
            self.exam_page.end_exam()

    def test_full_anti_cheat_workflow(self, driver):
        print("\n=== 开始完整防作弊测试工作流 ===")

        print("步骤 1: 登录系统")
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')
        assert login_page.is_login_success(), "登录失败"

        print("步骤 2: 进入考试")
        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)
        self.exam_page = exam_page

        print("步骤 3: 验证监控组件激活")
        Assertions.assert_monitor_active(exam_page)

        print("步骤 4: 测试切屏检测")
        exam_page.trigger_page_hidden()
        time.sleep(1)
        exam_page.trigger_page_visible()
        time.sleep(2)
        Assertions.assert_violation_warning_shown(exam_page)
        print("切屏检测通过")

        print("步骤 5: 测试复制粘贴检测")
        from selenium.webdriver.common.by import By
        from selenium.webdriver.common.keys import Keys
        question_area = driver.find_element(By.CSS_SELECTOR, ".exam-question-item")
        question_area.click()
        question_area.send_keys(Keys.CONTROL + 'c')
        time.sleep(1)
        alert_text = exam_page.wait_for_alert(timeout=3)
        assert alert_text is not None, "复制警告未出现"
        print("复制粘贴检测通过")

        print("=== 完整防作弊测试工作流完成 ===")

    def test_regression_suite(self, driver):
        print("\n=== 开始回归测试套件 ===")

        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')

        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)
        self.exam_page = exam_page

        test_results = {}

        print("测试 1: 切屏检测")
        try:
            exam_page.trigger_page_hidden()
            time.sleep(1)
            exam_page.trigger_page_visible()
            time.sleep(2)
            assert exam_page.is_violation_warning_shown()
            test_results['switch_screen'] = 'PASS'
        except Exception as e:
            test_results['switch_screen'] = f'FAIL: {str(e)}'

        print("测试 2: 监控状态")
        try:
            assert exam_page.is_monitor_active()
            test_results['monitor_status'] = 'PASS'
        except Exception as e:
            test_results['monitor_status'] = f'FAIL: {str(e)}'

        print("\n=== 回归测试结果 ===")
        for test_name, result in test_results.items():
            print(f"{test_name}: {result}")

        assert all('PASS' in result for result in test_results.values()), "回归测试存在失败用例"
