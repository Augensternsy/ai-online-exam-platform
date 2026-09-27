from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from base.base_page import BasePage
import time

class ExamPage(BasePage):
    EXAM_LIST_URL = "http://localhost:8000/#/paper/index"
    START_BUTTON = (By.CSS_SELECTOR, ".el-button--primary")
    SUBMIT_BUTTON = (By.XPATH, "//button[contains(text(), '提交')]")
    CONFIRM_BUTTON = (By.XPATH, "//button[contains(text(), '确定')]")
    VIOLATION_WARNING = (By.CSS_SELECTOR, ".el-message--warning")
    INTERRUPT_DIALOG = (By.CSS_SELECTOR, ".el-dialog__title")
    MONITOR_CONTAINER = (By.CSS_SELECTOR, ".monitor-container")
    MONITOR_STATUS = (By.CSS_SELECTOR, ".monitor-status")
    
    def navigate_to_exam_list(self):
        self.driver.get(self.EXAM_LIST_URL)
        return self

    def start_exam(self, exam_id):
        self.navigate_to_exam_list()
        time.sleep(2)
        
        start_buttons = self.driver.find_elements(*self.START_BUTTON)
        if start_buttons:
            start_buttons[0].click()
            time.sleep(3)
        
        return self

    def submit_exam(self):
        self.click_element(*self.SUBMIT_BUTTON)
        time.sleep(1)
        
        try:
            self.click_element(*self.CONFIRM_BUTTON)
        except:
            pass
        
        return self

    def end_exam(self):
        try:
            self.submit_exam()
        except:
            pass
        return self

    def is_monitor_active(self):
        return self.is_element_visible(*self.MONITOR_CONTAINER)

    def get_monitor_status(self):
        try:
            status_element = self.find_element(*self.MONITOR_STATUS)
            return status_element.text
        except:
            return None

    def is_violation_warning_shown(self):
        return self.is_element_visible(*self.VIOLATION_WARNING)

    def get_violation_warning_text(self):
        try:
            warning = self.find_element(*self.VIOLATION_WARNING)
            return warning.text
        except:
            return None

    def is_interrupt_dialog_shown(self):
        return self.is_element_visible(*self.INTERRUPT_DIALOG)

    def trigger_switch_screen(self):
        self.execute_script("""
            document.dispatchEvent(new Event('visibilitychange'));
        """)
        return self

    def trigger_page_hidden(self):
        self.execute_script("""
            Object.defineProperty(document, 'hidden', {value: true, writable: true});
            document.dispatchEvent(new Event('visibilitychange'));
        """)
        return self

    def trigger_page_visible(self):
        self.execute_script("""
            Object.defineProperty(document, 'hidden', {value: false, writable: true});
            document.dispatchEvent(new Event('visibilitychange'));
        """)
        return self

    def get_violation_count(self):
        try:
            badge = self.find_element(By.CSS_SELECTOR, ".el-badge__content")
            return int(badge.text)
        except:
            return 0

    def wait_for_violation_warning(self, timeout=5):
        return self.is_element_visible(*self.VIOLATION_WARNING, timeout=timeout)

    def wait_for_interrupt(self, timeout=10):
        return self.is_element_visible(*self.INTERRUPT_DIALOG, timeout=timeout)
