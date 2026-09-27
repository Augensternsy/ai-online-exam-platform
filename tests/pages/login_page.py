from selenium.webdriver.common.by import By
from base.base_page import BasePage

class LoginPage(BasePage):
    LOGIN_URL = "http://localhost:8000"
    
    USERNAME_INPUT = (By.CSS_SELECTOR, "input[placeholder='请输入用户名']")
    PASSWORD_INPUT = (By.CSS_SELECTOR, "input[placeholder='请输入密码']")
    LOGIN_BUTTON = (By.CSS_SELECTOR, "button[type='submit']")
    LOGOUT_BUTTON = (By.CSS_SELECTOR, ".el-dropdown-link")
    LOGOUT_MENU = (By.CSS_SELECTOR, ".el-dropdown-menu__item")
    
    def navigate(self):
        self.driver.get(self.LOGIN_URL)
        return self

    def input_username(self, username):
        self.input_text(*self.USERNAME_INPUT, username)
        return self

    def input_password(self, password):
        self.input_text(*self.PASSWORD_INPUT, password)
        return self

    def click_login(self):
        self.click_element(*self.LOGIN_BUTTON)
        return self

    def login(self, username, password):
        self.navigate()
        self.input_username(username)
        self.input_password(password)
        self.click_login()
        return self

    def logout(self):
        try:
            self.click_element(*self.LOGOUT_BUTTON)
            self.click_element(*self.LOGOUT_MENU)
        except:
            pass
        return self

    def is_login_success(self):
        return self.is_element_visible(By.CSS_SELECTOR, ".app-container")
