import time
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException


class AntiCheatSimulator:
    
    def __init__(self, driver):
        self.driver = driver
    
    def simulate_screen_switch(self, switch_count=1):
        for i in range(switch_count):
            self.driver.execute_script("""
                Object.defineProperty(document, 'hidden', {value: true, configurable: true});
                Object.defineProperty(document, 'visibilityState', {value: 'hidden', configurable: true});
                document.dispatchEvent(new Event('visibilitychange', {bubbles: true, cancelable: true}));
            """)
            print(f"[INFO] 第 {i+1} 次切屏事件已触发")
            time.sleep(1)
            
            self.driver.execute_script("""
                Object.defineProperty(document, 'hidden', {value: false, configurable: true});
                Object.defineProperty(document, 'visibilityState', {value: 'visible', configurable: true});
                document.dispatchEvent(new Event('visibilitychange', {bubbles: true, cancelable: true}));
            """)
            time.sleep(1)
        
        return switch_count
    
    def simulate_focus_loss(self):
        self.driver.execute_script("""
            window.dispatchEvent(new Event('blur'));
            window.dispatchEvent(new Event('focusout'));
        """)
        print("[INFO] 窗口失焦事件已触发")
    
    def simulate_focus_gain(self):
        self.driver.execute_script("""
            window.dispatchEvent(new Event('focus'));
            window.dispatchEvent(new Event('focusin'));
        """)
        print("[INFO] 窗口聚焦事件已触发")
    
    def simulate_network_disconnect(self):
        self.driver.execute_cdp_cmd('Network.enable', {})
        self.driver.execute_cdp_cmd('Network.emulateNetworkConditions', {
            'offline': True,
            'latency': 0,
            'downloadThroughput': 0,
            'uploadThroughput': 0
        })
        print("[INFO] 网络已断开")
    
    def simulate_network_reconnect(self):
        self.driver.execute_cdp_cmd('Network.emulateNetworkConditions', {
            'offline': False,
            'latency': 0,
            'downloadThroughput': -1,
            'uploadThroughput': -1
        })
        print("[INFO] 网络已恢复")
    
    def check_warning_message(self, timeout=10):
        try:
            warning_element = WebDriverWait(self.driver, timeout).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, ".el-message--warning, .warning-dialog, [class*='warning']"))
            )
            message_text = warning_element.text
            print(f"[PASS] 检测到警告信息: {message_text}")
            return True, message_text
        except TimeoutException:
            print("[FAIL] 未检测到警告信息")
            return False, None
    
    def check_exam_status(self, timeout=10):
        try:
            status_element = WebDriverWait(self.driver, timeout).until(
                EC.presence_of_element_located((By.CSS_SELECTOR, ".exam-status, [class*='status']"))
            )
            return status_element.text
        except TimeoutException:
            return None
    
    def get_console_logs(self):
        logs = self.driver.get_log('browser')
        anti_cheat_logs = [log for log in logs if 'anti-cheat' in log.get('message', '').lower() or 'cheat' in log.get('message', '').lower()]
        return anti_cheat_logs
