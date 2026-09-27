import pytest
import time
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from pages.login_page import LoginPage
from pages.exam_page import ExamPage
from utils.assertions import Assertions

class TestBoundary:
    def test_copy_paste_detection(self, driver):
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')

        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)

        question_area = driver.find_element(By.CSS_SELECTOR, ".exam-question-item")
        question_area.click()
        time.sleep(1)

        question_area.send_keys(Keys.CONTROL + 'c')
        time.sleep(1)

        alert_text = exam_page.wait_for_alert(timeout=3)
        assert alert_text is not None, "复制警告未出现"
        assert "禁止复制" in alert_text, f"警告文本不正确: {alert_text}"
        print("复制检测测试通过")

        question_area.send_keys(Keys.CONTROL + 'v')
        time.sleep(1)

        alert_text = exam_page.wait_for_alert(timeout=3)
        assert alert_text is not None, "粘贴警告未出现"
        assert "禁止粘贴" in alert_text, f"警告文本不正确: {alert_text}"
        print("粘贴检测测试通过")

    def test_camera_permission_denied(self, driver):
        from selenium.webdriver.chrome.options import Options
        
        chrome_options = Options()
        chrome_options.add_argument('--deny-permission-prompts')
        
        driver.quit()
        
        from selenium import webdriver
        from selenium.webdriver.chrome.service import Service
        from webdriver_manager.chrome import ChromeDriverManager
        
        driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=chrome_options
        )
        
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')

        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)

        permission_dialog = exam_page.is_element_visible(By.CSS_SELECTOR, ".permission-dialog-content", timeout=5)
        assert permission_dialog, "权限拒绝对话框未显示"
        print("摄像头权限拒绝测试通过")

    def test_exam_timeout(self, driver):
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')

        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)

        exam_page.execute_script("""
            window.remainTime = 0;
        """)
        time.sleep(2)

        Assertions.assert_exam_interrupted(exam_page)
        print("考试超时测试通过")

    def test_network_disconnect(self, driver):
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')

        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)

        driver.execute_cdp_cmd('Network.enable', {})
        driver.execute_cdp_cmd('Network.emulateNetworkConditions', {
            'offline': True,
            'latency': 0,
            'downloadThroughput': 0,
            'uploadThroughput': 0
        })
        time.sleep(3)

        driver.execute_cdp_cmd('Network.emulateNetworkConditions', {
            'offline': False,
            'latency': 0,
            'downloadThroughput': -1,
            'uploadThroughput': -1
        })
        time.sleep(2)

        print("网络断开测试通过")
