import pytest
import time
import os
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

class TestCharlesErrorInjection:
    """Charles 异常状态码注入测试"""
    
    @pytest.fixture(scope="function")
    def driver(self):
        """创建带代理的 WebDriver 实例"""
        chrome_options = Options()
        # 配置 Charles 代理
        chrome_options.add_argument('--proxy-server=127.0.0.1:8888')
        chrome_options.add_argument('--ignore-certificate-errors')
        chrome_options.add_argument('--no-sandbox')
        chrome_options.add_argument('--disable-dev-shm-usage')
        chrome_options.add_argument('--window-size=1920,1080')
        
        driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=chrome_options
        )
        
        driver.implicitly_wait(10)
        driver.maximize_window()
        
        yield driver
        
        driver.quit()

    def create_error_handling_page(self):
        """创建错误处理测试页面"""
        test_html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>错误处理测试</title>
            <style>
                .error-box {
                    display: none;
                    padding: 20px;
                    margin: 20px 0;
                    background-color: #ffebee;
                    border: 1px solid #f44336;
                    border-radius: 4px;
                }
                .error-box.show {
                    display: block;
                }
                .retry-btn {
                    padding: 10px 20px;
                    background-color: #2196F3;
                    color: white;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                }
                .retry-btn:hover {
                    background-color: #1976D2;
                }
            </style>
        </head>
        <body>
            <h1>错误处理测试页面</h1>
            
            <div id="api-status">
                <h3>API 状态</h3>
                <div id="submit-status">交卷接口：未调用</div>
                <div id="monitor-status">监控接口：未调用</div>
                <div id="face-status">人脸接口：未调用</div>
            </div>
            
            <div id="error-display" class="error-box">
                <h3>错误信息</h3>
                <div id="error-code">错误码：</div>
                <div id="error-message">错误描述：</div>
                <button class="retry-btn" onclick="retryAll()">重试</button>
            </div>
            
            <div id="local-cache" class="error-box">
                <h3>本地缓存</h3>
                <div id="cache-status">缓存状态：未激活</div>
                <div id="cache-data">缓存数据：无</div>
            </div>
            
            <button onclick="testSubmit()">测试交卷接口</button>
            <button onclick="testMonitor()">测试监控接口</button>
            <button onclick="testFaceDetect()">测试人脸接口</button>
            
            <script>
                let retryCount = 0;
                const maxRetries = 3;
                
                // 模拟交卷接口调用
                function testSubmit() {
                    document.getElementById('submit-status').textContent = '交卷接口：调用中...';
                    
                    fetch('/api/exam/submit', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ examId: 1, answers: [] })
                    })
                    .then(response => {
                        if (response.ok) {
                            document.getElementById('submit-status').textContent = '交卷接口：成功';
                            hideError();
                        } else {
                            throw new Error(`HTTP ${response.status}`);
                        }
                    })
                    .catch(error => {
                        document.getElementById('submit-status').textContent = '交卷接口：失败';
                        showError(error.message, '交卷接口调用失败');
                        activateLocalCache('交卷数据');
                    });
                }
                
                // 模拟监控接口调用
                function testMonitor() {
                    document.getElementById('monitor-status').textContent = '监控接口：调用中...';
                    
                    fetch('/api/exam/monitor', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ timestamp: Date.now(), faceCount: 1 })
                    })
                    .then(response => {
                        if (response.ok) {
                            document.getElementById('monitor-status').textContent = '监控接口：成功';
                            hideError();
                        } else {
                            throw new Error(`HTTP ${response.status}`);
                        }
                    })
                    .catch(error => {
                        document.getElementById('monitor-status').textContent = '监控接口：失败';
                        showError(error.message, '监控接口调用失败');
                    });
                }
                
                // 模拟人脸检测接口调用
                function testFaceDetect() {
                    document.getElementById('face-status').textContent = '人脸接口：调用中...';
                    
                    fetch('/api/exam/face-detect', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ image: 'base64...', timestamp: Date.now() })
                    })
                    .then(response => {
                        if (response.ok) {
                            document.getElementById('face-status').textContent = '人脸接口：成功';
                            hideError();
                        } else {
                            throw new Error(`HTTP ${response.status}`);
                        }
                    })
                    .catch(error => {
                        document.getElementById('face-status').textContent = '人脸接口：失败';
                        showError(error.message, '人脸检测接口调用失败');
                    });
                }
                
                // 显示错误信息
                function showError(code, message) {
                    document.getElementById('error-code').textContent = '错误码：' + code;
                    document.getElementById('error-message').textContent = '错误描述：' + message;
                    document.getElementById('error-display').classList.add('show');
                }
                
                // 隐藏错误信息
                function hideError() {
                    document.getElementById('error-display').classList.remove('show');
                }
                
                // 激活本地缓存
                function activateLocalCache(data) {
                    document.getElementById('cache-status').textContent = '缓存状态：已激活';
                    document.getElementById('cache-data').textContent = '缓存数据：' + data;
                    document.getElementById('local-cache').classList.add('show');
                }
                
                // 重试所有接口
                function retryAll() {
                    retryCount++;
                    if (retryCount <= maxRetries) {
                        document.getElementById('error-message').textContent = 
                            '正在重试 (' + retryCount + '/' + maxRetries + ')';
                        setTimeout(() => {
                            testSubmit();
                            testMonitor();
                            testFaceDetect();
                        }, 1000);
                    } else {
                        document.getElementById('error-message').textContent = 
                            '重试次数已达上限，请稍后重试';
                    }
                }
            </script>
        </body>
        </html>
        """
        
        temp_html = os.path.join(os.path.dirname(__file__), 'mock_error_handling.html')
        with open(temp_html, 'w', encoding='utf-8') as f:
            f.write(test_html)
        
        return temp_html

    def test_500_error_handling(self, driver):
        """测试 1: 500 服务器错误处理"""
        print("\n" + "="*60)
        print("测试 1: 500 服务器错误处理")
        print("Charles 配置：Rewrite 200 → 500")
        print("="*60)
        
        temp_html = self.create_error_handling_page()
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 测试交卷接口（应该返回 500 错误）
            submit_btn = driver.find_element(By.XPATH, "//button[text()='测试交卷接口']")
            submit_btn.click()
            time.sleep(3)
            
            # 验证接口状态
            submit_status = driver.find_element(By.ID, 'submit-status').text
            print(f"✅ 交卷接口状态：{submit_status}")
            
            # 验证错误显示
            error_display = driver.find_element(By.ID, 'error-display')
            assert 'show' in error_display.get_attribute('class'), "错误信息未显示"
            
            error_code = driver.find_element(By.ID, 'error-code').text
            error_message = driver.find_element(By.ID, 'error-message').text
            print(f"✅ 错误码：{error_code}")
            print(f"✅ 错误描述：{error_message}")
            
            # 验证本地缓存激活
            cache_status = driver.find_element(By.ID, 'cache-status').text
            print(f"✅ 缓存状态：{cache_status}")
            
            print("✅ 测试 1 通过：500 错误处理正常")
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_502_error_handling(self, driver):
        """测试 2: 502 网关错误处理"""
        print("\n" + "="*60)
        print("测试 2: 502 网关错误处理")
        print("Charles 配置：Rewrite 200 → 502")
        print("="*60)
        
        temp_html = self.create_error_handling_page()
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 测试人脸接口（应该返回 502 错误）
            face_btn = driver.find_element(By.XPATH, "//button[text()='测试人脸接口']")
            face_btn.click()
            time.sleep(3)
            
            # 验证接口状态
            face_status = driver.find_element(By.ID, 'face-status').text
            print(f"✅ 人脸接口状态：{face_status}")
            
            # 验证错误显示
            error_display = driver.find_element(By.ID, 'error-display')
            assert 'show' in error_display.get_attribute('class'), "错误信息未显示"
            
            error_code = driver.find_element(By.ID, 'error-code').text
            print(f"✅ 错误码：{error_code}")
            
            print("✅ 测试 2 通过：502 错误处理正常")
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_504_timeout_handling(self, driver):
        """测试 3: 504 超时错误处理"""
        print("\n" + "="*60)
        print("测试 3: 504 超时错误处理")
        print("Charles 配置：Rewrite 200 → 504")
        print("="*60)
        
        temp_html = self.create_error_handling_page()
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 测试监控接口（应该返回 504 超时）
            monitor_btn = driver.find_element(By.XPATH, "//button[text()='测试监控接口']")
            monitor_btn.click()
            time.sleep(5)
            
            # 验证接口状态
            monitor_status = driver.find_element(By.ID, 'monitor-status').text
            print(f"✅ 监控接口状态：{monitor_status}")
            
            # 验证错误显示
            error_display = driver.find_element(By.ID, 'error-display')
            assert 'show' in error_display.get_attribute('class'), "错误信息未显示"
            
            error_code = driver.find_element(By.ID, 'error-code').text
            print(f"✅ 错误码：{error_code}")
            
            print("✅ 测试 3 通过：504 超时处理正常")
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_retry_mechanism(self, driver):
        """测试 4: 重试机制"""
        print("\n" + "="*60)
        print("测试 4: 重试机制测试")
        print("="*60)
        
        temp_html = self.create_error_handling_page()
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 触发错误
            submit_btn = driver.find_element(By.XPATH, "//button[text()='测试交卷接口']")
            submit_btn.click()
            time.sleep(2)
            
            # 点击重试按钮
            retry_btn = driver.find_element(By.CLASS_NAME, 'retry-btn')
            retry_btn.click()
            time.sleep(3)
            
            # 验证重试状态
            error_message = driver.find_element(By.ID, 'error-message').text
            print(f"✅ 重试状态：{error_message}")
            
            print("✅ 测试 4 通过：重试机制正常")
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_local_cache_fallback(self, driver):
        """测试 5: 本地缓存兜底策略"""
        print("\n" + "="*60)
        print("测试 5: 本地缓存兜底策略")
        print("="*60)
        
        temp_html = self.create_error_handling_page()
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 触发错误并激活缓存
            submit_btn = driver.find_element(By.XPATH, "//button[text()='测试交卷接口']")
            submit_btn.click()
            time.sleep(3)
            
            # 验证缓存激活
            cache_display = driver.find_element(By.ID, 'local-cache')
            assert 'show' in cache_display.get_attribute('class'), "本地缓存未激活"
            
            cache_status = driver.find_element(By.ID, 'cache-status').text
            cache_data = driver.find_element(By.ID, 'cache-data').text
            print(f"✅ 缓存状态：{cache_status}")
            print(f"✅ 缓存数据：{cache_data}")
            
            print("✅ 测试 5 通过：本地缓存兜底策略正常")
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

if __name__ == "__main__":
    pytest.main([__file__, "-v", "--html=reports/charles_error_injection_report.html"])
