from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException

class BasePage:
    def __init__(self, driver):
        self.driver = driver
        self.wait = WebDriverWait(driver, 10)

    def find_element(self, by, value):
        return self.wait.until(EC.presence_of_element_located((by, value)))

    def find_elements(self, by, value):
        return self.wait.until(EC.presence_of_all_elements_located((by, value)))

    def click_element(self, by, value):
        element = self.find_element(by, value)
        element.click()
        return element

    def input_text(self, by, value, text):
        element = self.find_element(by, value)
        element.clear()
        element.send_keys(text)
        return element

    def get_text(self, by, value):
        element = self.find_element(by, value)
        return element.text

    def is_element_visible(self, by, value, timeout=5):
        try:
            self.wait.until(EC.visibility_of_element_located((by, value)), timeout=timeout)
            return True
        except TimeoutException:
            return False

    def execute_script(self, script):
        return self.driver.execute_script(script)

    def get_current_url(self):
        return self.driver.current_url

    def wait_for_alert(self, timeout=5):
        try:
            alert = self.wait.until(EC.alert_is_present(), timeout=timeout)
            alert_text = alert.text
            alert.accept()
            return alert_text
        except TimeoutException:
            return None
