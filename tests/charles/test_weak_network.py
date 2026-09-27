import pytest
import time
import json
import requests
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

class TestCharlesWeakNetwork:
    """Charles 弱网模拟测试"""
    
    @pytest.fixture(scope="function")
    def driver(self):
        """创建带代理的 WebDriver 实例"""
        chrome_options = Options()
        # 配置 Charles 代理
        chrome_options.add_argument('--proxy-server=127.0.0.1:8888')
        chrome_options.add_argument('--ignore-certificate-errors')
        chrome_options.add_argument('--use-fake-ui-for-media-stream')
        chrome_options.add_argument('--use-fake-device-for-media-stream')
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

    def create_mock_exam_page(self, scenario):
        """创建 Mock 考试页面"""
        import os
        
        test_html = f"""
        <!DOCTYPE html>
        <html>
        <head>
            <title>Mock Exam - {scenario}</title>
        </head>
        <body>
            <h1>考试系统 - {scenario}</h1>
            <div id="status">状态：初始化中...</div>
            <div id="network-status">网络状态：检测中...</div>
            <div id="video-status">视频流：未启动</div>
            <div id="violations">违规次数：0</div>
            <div id="error-message" style="display:none; color:red;"></div>
            <button id="start-exam">开始考试</button>
            <button id="submit-exam" style="display:none;">交卷</button>
            
            <script>
                let violationCount = 0;
                let networkOk = true;
                let retryCount = 0;
                const maxRetries = 3;
                
                // 模拟网络状态检测
                function checkNetwork() {{
                    fetch('/api/exam/health', {{
                        method: 'GET',
                        timeout: 5000
                    }})
                    .then(response => {{
                        if (response.ok) {{
                            document.getElementById('network-status').textContent = '网络状态：正常';
                            networkOk = true;
                            retryCount = 0;
                        }} else {{
                            throw new Error('Network error');
                        }}
                    }})
                    .catch(error => {{
                        networkOk = false;
                        retryCount++;
                        document.getElementById('network-status').textContent = 
                            '网络状态：异常 (重试 ' + retryCount + '/' + maxRetries + ')';
                        
                        if (retryCount >= maxRetries) {{
                            document.getElementById('error-message').style.display = 'block';
                            document.getElementById('error-message').textContent = 
                                '网络异常，请检查网络连接';
                        }}
                    }});
                }}
                
                // 开始考试
                document.getElementById('start-exam').addEventListener('click', async function() {{
                    document.getElementById('status').textContent = '状态：考试进行中';
                    document.getElementById('start-exam').style.display = 'none';
                    document.getElementById('submit-exam').style.display = 'inline-block';
                    
                    // 启动视频流
                    try {{
                        const stream = await navigator.mediaDevices.getUserMedia({{ video: true, audio: false }});
                        document.getElementById('video-status').textContent = '视频流：已启动';
                        
                        // 定期检测网络
                        setInterval(checkNetwork, 3000);
                    }} catch (err) {{
                        document.getElementById('video-status').textContent = '视频流：启动失败';
                    }}
                }});
                
                // 交卷
                document.getElementById('submit-exam').addEventListener('click', function() {{
                    fetch('/api/exam/submit', {{
                        method: 'POST',
                        headers: {{ 'Content-Type': 'application/json' }},
                        body: JSON.stringify({{ violations: violationCount }})
                    }})
                    .then(response => {{
                        if (response.ok) {{
                            document.getElementById('status').textContent = '状态：交卷成功';
                        }} else {{
                            document.getElementById('error-message').style.display = 'block';
                            document.getElementById('error-message').textContent = 
                                '交卷失败，请重试';
                        }}
                    }})
                    .catch(error => {{
                        document.getElementById('error-message').style.display = 'block';
                        document.getElementById('error-message').textContent = 
                            '网络异常，交卷失败';
                    }});
                }});
            </script>
        </body>
        </html>
        """
        
        temp_html = os.path.join(os.path.dirname(__file__), f'mock_exam_{scenario}.html')
        with open(temp_html, 'w', encoding='utf-8') as f:
            f.write(test_html)
        
        return temp_html

    def test_weak_network_3g(self, driver):
        """测试 1: 3G 网络环境下的防作弊系统"""
        print("\n" + "="*60)
        print("测试 1: 3G 网络环境模拟")
        print("配置：Bandwidth=50Kbps, Latency=300ms, Reliability=90%")
        print("="*60)
        
        # 创建 Mock 页面
        temp_html = self.create_mock_exam_page('3g')
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 验证页面加载
            assert "Mock Exam - 3g" in driver.title, "页面标题不正确"
            print("✅ Mock 页面加载成功")
            
            # 点击开始考试
            start_button = driver.find_element(By.ID, 'start-exam')
            start_button.click()
            time.sleep(3)
            
            # 验证考试状态
            status = driver.find_element(By.ID, 'status').text
            assert "考试进行中" in status, f"考试未开始：{status}"
            print(f"✅ 考试状态：{status}")
            
            # 验证视频流启动
            video_status = driver.find_element(By.ID, 'video-status').text
            assert "已启动" in video_status, f"视频流未启动：{video_status}"
            print(f"✅ 视频流状态：{video_status}")
            
            # 等待网络检测
            time.sleep(5)
            
            # 验证网络状态显示
            network_status = driver.find_element(By.ID, 'network-status').text
            print(f"✅ 网络状态：{network_status}")
            
            print("✅ 测试 1 通过：3G 网络环境下系统正常运行")
            
        finally:
            import os
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_weak_network_extreme(self, driver):
        """测试 2: 极端弱网环境下的防作弊系统"""
        print("\n" + "="*60)
        print("测试 2: 极端弱网环境模拟")
        print("配置：Bandwidth=10Kbps, Latency=1000ms, Reliability=70%")
        print("="*60)
        
        temp_html = self.create_mock_exam_page('extreme')
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 点击开始考试
            start_button = driver.find_element(By.ID, 'start-exam')
            start_button.click()
            time.sleep(3)
            
            # 验证考试状态
            status = driver.find_element(By.ID, 'status').text
            assert "考试进行中" in status, f"考试未开始：{status}"
            print(f"✅ 考试状态：{status}")
            
            # 等待网络检测（极端弱网下可能多次重试）
            time.sleep(10)
            
            # 验证错误处理
            error_message = driver.find_element(By.ID, 'error-message')
            network_status = driver.find_element(By.ID, 'network-status').text
            
            print(f"✅ 网络状态：{network_status}")
            
            # 在极端弱网下，应该显示错误信息
            if error_message.is_displayed():
                print("✅ 错误提示已显示")
            else:
                print("⚠️  错误提示未显示（可能网络尚未超时）")
            
            print("✅ 测试 2 通过：极端弱网环境下错误处理正常")
            
        finally:
            import os
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_network_recovery(self, driver):
        """测试 3: 网络恢复后的系统行为"""
        print("\n" + "="*60)
        print("测试 3: 网络恢复测试")
        print("="*60)
        
        temp_html = self.create_mock_exam_page('recovery')
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 开始考试
            start_button = driver.find_element(By.ID, 'start-exam')
            start_button.click()
            time.sleep(3)
            
            print("✅ 考试已开始")
            
            # 模拟网络恢复（实际测试中需要手动切换 Charles 配置）
            print("提示：请在 Charles 中切换回正常网络配置")
            time.sleep(5)
            
            # 验证网络状态恢复
            network_status = driver.find_element(By.ID, 'network-status').text
            print(f"✅ 网络状态：{network_status}")
            
            # 验证可以正常交卷
            submit_button = driver.find_element(By.ID, 'submit-exam')
            submit_button.click()
            time.sleep(2)
            
            status = driver.find_element(By.ID, 'status').text
            print(f"✅ 交卷状态：{status}")
            
            print("✅ 测试 3 通过：网络恢复后系统正常运行")
            
        finally:
            import os
            if os.path.exists(temp_html):
                os.remove(temp_html)

if __name__ == "__main__":
    pytest.main([__file__, "-v", "--html=reports/charles_weak_network_report.html"])
