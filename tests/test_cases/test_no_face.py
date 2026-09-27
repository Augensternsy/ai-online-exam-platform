import pytest
import time
import os
from selenium.webdriver.common.by import By
from pages.login_page import LoginPage
from pages.exam_page import ExamPage
from utils.assertions import Assertions

class TestNoFace:
    def setup_method(self, method):
        self.video_dir = os.path.join(os.path.dirname(__file__), '..', 'test_videos')
        os.makedirs(self.video_dir, exist_ok=True)

    def test_no_face_detection(self, driver):
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')
        assert login_page.is_login_success(), "登录失败"

        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)

        Assertions.assert_monitor_active(exam_page)

        time.sleep(5)

        exam_page.trigger_switch_screen()
        time.sleep(2)

        Assertions.assert_violation_warning_shown(exam_page)

        warning_text = exam_page.get_violation_warning_text()
        assert warning_text is not None, "未获取到警告文本"
        print(f"警告文本: {warning_text}")

    def test_no_face_auto_submit(self, driver):
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')

        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)

        for i in range(5):
            exam_page.trigger_switch_screen()
            time.sleep(2)

        Assertions.assert_exam_interrupted(exam_page)

        print("无人场景自动交卷测试通过")
