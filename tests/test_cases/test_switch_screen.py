import pytest
import time
from pages.login_page import LoginPage
from pages.exam_page import ExamPage
from utils.assertions import Assertions

class TestSwitchScreen:
    @pytest.mark.parametrize("switch_times", [1, 3, 5])
    def test_switch_screen_detection(self, driver, switch_times):
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')
        assert login_page.is_login_success(), "登录失败"

        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)

        Assertions.assert_monitor_active(exam_page)

        for i in range(switch_times):
            exam_page.trigger_page_hidden()
            time.sleep(1)
            exam_page.trigger_page_visible()
            time.sleep(1)

        if switch_times < 5:
            Assertions.assert_violation_warning_shown(exam_page)
            warning_text = exam_page.get_violation_warning_text()
            assert warning_text is not None, "未获取到警告文本"
            print(f"切屏{switch_times}次警告文本: {warning_text}")
        else:
            Assertions.assert_exam_interrupted(exam_page)
            print(f"切屏{switch_times}次自动交卷测试通过")

    def test_switch_screen_boundary(self, driver):
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')

        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)

        for i in range(4):
            exam_page.trigger_page_hidden()
            time.sleep(1)
            exam_page.trigger_page_visible()
            time.sleep(1)

        Assertions.assert_violation_warning_shown(exam_page)

        exam_page.trigger_page_hidden()
        time.sleep(1)
        exam_page.trigger_page_visible()
        time.sleep(2)

        Assertions.assert_exam_interrupted(exam_page)
        print("切屏边界测试通过")

    def test_rapid_switch_screen(self, driver):
        login_page = LoginPage(driver)
        login_page.login('testuser', 'testpassword')

        exam_page = ExamPage(driver)
        exam_page.start_exam(1)
        time.sleep(3)

        for i in range(10):
            exam_page.trigger_page_hidden()
            exam_page.trigger_page_visible()
            time.sleep(0.1)

        time.sleep(2)
        Assertions.assert_exam_interrupted(exam_page)
        print("快速切屏测试通过")
