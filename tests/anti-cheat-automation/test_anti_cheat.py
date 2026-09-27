import pytest
import time
import os
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from browser_setup import AntiCheatBrowser
from anti_cheat_simulator import AntiCheatSimulator


BASE_URL = "http://localhost:8080"
STUDENT_USERNAME = "test_student"
STUDENT_PASSWORD = "123456"


@pytest.fixture(scope="function")
def browser_with_fake_video():
    anti_cheat_browser = AntiCheatBrowser(config={"headless": False})
    driver = anti_cheat_browser.create_driver_with_fake_video("no_face.y4m")
    yield driver
    anti_cheat_browser.quit()


@pytest.fixture(scope="function")
def normal_browser():
    anti_cheat_browser = AntiCheatBrowser(config={"headless": False})
    driver = anti_cheat_browser.create_normal_driver()
    yield driver
    anti_cheat_browser.quit()


def login(driver, username=STUDENT_USERNAME, password=STUDENT_PASSWORD):
    driver.get(f"{BASE_URL}/#/login")
    WebDriverWait(driver, 10).until(
        EC.presence_of_element_located((By.CSS_SELECTOR, "input[placeholder='用户名']"))
    ).send_keys(username)
    
    driver.find_element(By.CSS_SELECTOR, "input[placeholder='密码']").send_keys(password)
    driver.find_element(By.CSS_SELECTOR, "button[type='submit']").click()
    
    WebDriverWait(driver, 10).until(
        EC.url_contains("/dashboard")
    )
    print(f"[INFO] 用户 {username} 登录成功")


class TestNoFaceDetection:
    
    @pytest.mark.anti_cheat
    @pytest.mark.critical
    def test_no_face_detection(self, browser_with_fake_video):
        driver = browser_with_fake_video
        simulator = AntiCheatSimulator(driver)
        
        login(driver)
        
        driver.get(f"{BASE_URL}/#/exam/start")
        time.sleep(3)
        
        warning_detected, message = simulator.check_warning_message(timeout=15)
        
        assert warning_detected, "无人场景未触发防作弊警告"
        assert any(keyword in message.lower() for keyword in ["人脸", "无人", "离开"]), f"警告信息不匹配: {message}"


class TestMultipleFaceDetection:
    
    @pytest.mark.anti_cheat
    @pytest.mark.critical
    def test_multiple_faces_detection(self, browser_with_fake_video):
        anti_cheat_browser = AntiCheatBrowser(config={"headless": False})
        driver = anti_cheat_browser.create_driver_with_fake_video("multiple_faces.y4m")
        simulator = AntiCheatSimulator(driver)
        
        try:
            login(driver)
            
            driver.get(f"{BASE_URL}/#/exam/start")
            time.sleep(3)
            
            warning_detected, message = simulator.check_warning_message(timeout=15)
            
            assert warning_detected, "多人场景未触发防作弊警告"
            assert any(keyword in message.lower() for keyword in ["多人", "作弊"]), f"警告信息不匹配: {message}"
        finally:
            anti_cheat_browser.quit()


class TestScreenSwitch:
    
    @pytest.mark.anti_cheat
    @pytest.mark.parametrize("switch_count,expected_warning", [
        (1, True),
        (3, True),
        (5, True),
    ])
    def test_screen_switch_detection(self, normal_browser, switch_count, expected_warning):
        driver = normal_browser
        simulator = AntiCheatSimulator(driver)
        
        login(driver)
        
        driver.get(f"{BASE_URL}/#/exam/start")
        time.sleep(3)
        
        simulator.simulate_screen_switch(switch_count=switch_count)
        
        warning_detected, message = simulator.check_warning_message(timeout=10)
        
        assert warning_detected == expected_warning, f"切屏 {switch_count} 次，预期警告={expected_warning}，实际警告={warning_detected}"


class TestNetworkDisconnect:
    
    @pytest.mark.anti_cheat
    def test_network_disconnect_during_exam(self, normal_browser):
        driver = normal_browser
        simulator = AntiCheatSimulator(driver)
        
        login(driver)
        
        driver.get(f"{BASE_URL}/#/exam/start")
        time.sleep(3)
        
        simulator.simulate_network_disconnect()
        time.sleep(5)
        
        simulator.simulate_network_reconnect()
        time.sleep(3)
        
        warning_detected, message = simulator.check_warning_message(timeout=10)
        
        assert warning_detected, "断网场景未触发防作弊警告"


class TestFocusLoss:
    
    @pytest.mark.anti_cheat
    def test_window_focus_loss(self, normal_browser):
        driver = normal_browser
        simulator = AntiCheatSimulator(driver)
        
        login(driver)
        
        driver.get(f"{BASE_URL}/#/exam/start")
        time.sleep(3)
        
        simulator.simulate_focus_loss()
        time.sleep(2)
        
        simulator.simulate_focus_gain()
        time.sleep(2)
        
        warning_detected, message = simulator.check_warning_message(timeout=10)
        
        assert warning_detected, "窗口失焦未触发防作弊警告"


class TestAntiCheatIntegration:
    
    @pytest.mark.anti_cheat
    @pytest.mark.integration
    def test_combined_cheat_scenarios(self, normal_browser):
        driver = normal_browser
        simulator = AntiCheatSimulator(driver)
        
        login(driver)
        
        driver.get(f"{BASE_URL}/#/exam/start")
        time.sleep(3)
        
        simulator.simulate_screen_switch(switch_count=2)
        time.sleep(2)
        
        simulator.simulate_focus_loss()
        time.sleep(2)
        simulator.simulate_focus_gain()
        time.sleep(2)
        
        logs = simulator.get_console_logs()
        
        assert len(logs) > 0, "防作弊系统未记录任何日志"
        print(f"[INFO] 捕获到 {len(logs)} 条防作弊日志")
