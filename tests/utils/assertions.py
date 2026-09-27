from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException
import time

class Assertions:
    @staticmethod
    def assert_element_visible(driver, by, value, timeout=5, message="元素未显示"):
        try:
            WebDriverWait(driver, timeout).until(
                EC.visibility_of_element_located((by, value))
            )
            return True
        except TimeoutException:
            raise AssertionError(message)

    @staticmethod
    def assert_element_not_visible(driver, by, value, timeout=5, message="元素意外显示"):
        try:
            WebDriverWait(driver, timeout).until(
                EC.invisibility_of_element_located((by, value))
            )
            return True
        except TimeoutException:
            raise AssertionError(message)

    @staticmethod
    def assert_text_contains(driver, by, value, expected_text, timeout=5, message="文本不匹配"):
        try:
            element = WebDriverWait(driver, timeout).until(
                EC.presence_of_element_located((by, value))
            )
            assert expected_text in element.text, f"{message}: 期望包含 '{expected_text}', 实际 '{element.text}'"
            return True
        except TimeoutException:
            raise AssertionError(f"{message}: 元素未找到")

    @staticmethod
    def assert_alert_present(driver, timeout=5, message="警告框未出现"):
        try:
            alert = WebDriverWait(driver, timeout).until(EC.alert_is_present())
            alert.accept()
            return True
        except TimeoutException:
            raise AssertionError(message)

    @staticmethod
    def assert_violation_warning_shown(exam_page, timeout=5):
        assert exam_page.wait_for_violation_warning(timeout), "违规警告未显示"

    @staticmethod
    def assert_exam_interrupted(exam_page, timeout=10):
        assert exam_page.wait_for_interrupt(timeout), "考试未中断"

    @staticmethod
    def assert_violation_count(exam_page, expected_count, timeout=5):
        time.sleep(timeout)
        actual_count = exam_page.get_violation_count()
        assert actual_count == expected_count, f"违规次数不匹配: 期望 {expected_count}, 实际 {actual_count}"

    @staticmethod
    def assert_monitor_active(exam_page):
        assert exam_page.is_monitor_active(), "监控组件未激活"
