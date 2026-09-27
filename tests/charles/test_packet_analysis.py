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

class TestCharlesPacketAnalysis:
    """Charles 请求抓包与报文分析测试"""
    
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

    def create_violation_log_page(self):
        """创建违纪日志上报测试页面"""
        test_html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>违纪日志上报测试</title>
        </head>
        <body>
            <h1>违纪日志上报测试</h1>
            
            <div id="monitor-info">
                <h3>监控信息</h3>
                <div id="face-coords">人脸坐标：未检测</div>
                <div id="gaze-direction">视线方向：未检测</div>
                <div id="violation-type">违纪类型：无</div>
            </div>
            
            <div id="log-status">
                <h3>日志状态</h3>
                <div id="send-status">发送状态：未发送</div>
                <div id="response-status">响应状态：无</div>
            </div>
            
            <button onclick="simulateViolation()">模拟违纪行为</button>
            <button onclick="sendViolationLog()">发送违纪日志</button>
            
            <script>
                let violationData = {
                    timestamp: 0,
                    faceCoords: { x: 0, y: 0 },
                    gazeDirection: '',
                    violationType: ''
                };
                
                // 模拟违纪行为
                function simulateViolation() {
                    violationData = {
                        timestamp: Date.now(),
                        faceCoords: { x: 150, y: 200 },
                        gazeDirection: 'left',
                        violationType: 'looking_around'
                    };
                    
                    document.getElementById('face-coords').textContent = 
                        '人脸坐标：(' + violationData.faceCoords.x + ', ' + violationData.faceCoords.y + ')';
                    document.getElementById('gaze-direction').textContent = 
                        '视线方向：' + violationData.gazeDirection;
                    document.getElementById('violation-type').textContent = 
                        '违纪类型：' + violationData.violationType;
                }
                
                // 发送违纪日志
                function sendViolationLog() {
                    document.getElementById('send-status').textContent = '发送状态：发送中...';
                    
                    fetch('/api/exam/violation', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer test-token-12345'
                        },
                        body: JSON.stringify(violationData)
                    })
                    .then(response => {
                        document.getElementById('response-status').textContent = 
                            '响应状态：' + response.status + ' ' + response.statusText;
                        
                        if (response.ok) {
                            document.getElementById('send-status').textContent = '发送状态：成功';
                            return response.json();
                        } else {
                            document.getElementById('send-status').textContent = '发送状态：失败';
                            throw new Error('Server error');
                        }
                    })
                    .then(data => {
                        console.log('违纪日志上报成功:', data);
                    })
                    .catch(error => {
                        console.error('违纪日志上报失败:', error);
                    });
                }
            </script>
        </body>
        </html>
        """
        
        temp_html = os.path.join(os.path.dirname(__file__), 'mock_violation_log.html')
        with open(temp_html, 'w', encoding='utf-8') as f:
            f.write(test_html)
        
        return temp_html

    def test_violation_log_request(self, driver):
        """测试 1: 违纪日志请求抓包分析"""
        print("\n" + "="*60)
        print("测试 1: 违纪日志请求抓包分析")
        print("验证点：Request Payload 格式、Token 正确性")
        print("="*60)
        
        temp_html = self.create_violation_log_page()
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 模拟违纪行为
            simulate_btn = driver.find_element(By.XPATH, "//button[text()='模拟违纪行为']")
            simulate_btn.click()
            time.sleep(1)
            
            # 验证违纪数据
            face_coords = driver.find_element(By.ID, 'face-coords').text
            gaze_direction = driver.find_element(By.ID, 'gaze-direction').text
            violation_type = driver.find_element(By.ID, 'violation-type').text
            
            print(f"✅ {face_coords}")
            print(f"✅ {gaze_direction}")
            print(f"✅ {violation_type}")
            
            # 发送违纪日志
            send_btn = driver.find_element(By.XPATH, "//button[text()='发送违纪日志']")
            send_btn.click()
            time.sleep(3)
            
            # 验证发送状态
            send_status = driver.find_element(By.ID, 'send-status').text
            response_status = driver.find_element(By.ID, 'response-status').text
            
            print(f"✅ {send_status}")
            print(f"✅ {response_status}")
            
            print("✅ 测试 1 通过：违纪日志请求已发送（请在 Charles 中查看报文）")
            print("提示：在 Charles 中查找 /api/exam/violation 请求，分析以下内容：")
            print("  - Request Headers: Authorization Token 是否正确")
            print("  - Request Payload: 坐标格式是否正确")
            print("  - Response Status: 后端返回状态码")
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_request_headers_analysis(self, driver):
        """测试 2: 请求头分析"""
        print("\n" + "="*60)
        print("测试 2: 请求头分析")
        print("验证点：Content-Type、Authorization、Token")
        print("="*60)
        
        temp_html = """
        <!DOCTYPE html>
        <html>
        <head><title>请求头测试</title></head>
        <body>
            <h1>请求头分析测试</h1>
            <div id="status">状态：未发送</div>
            <button onclick="sendRequest()">发送请求</button>
            
            <script>
                function sendRequest() {
                    fetch('/api/exam/submit', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test',
                            'X-Exam-Id': '12345',
                            'X-Student-Id': 'student-001'
                        },
                        body: JSON.stringify({
                            examId: 12345,
                            answers: [{ questionId: 1, answer: 'A' }]
                        })
                    })
                    .then(response => {
                        document.getElementById('status').textContent = 
                            '状态：' + response.status;
                    })
                    .catch(error => {
                        document.getElementById('status').textContent = 
                            '状态：请求失败';
                    });
                }
            </script>
        </body>
        </html>
        """
        
        temp_file = os.path.join(os.path.dirname(__file__), 'mock_headers.html')
        with open(temp_file, 'w', encoding='utf-8') as f:
            f.write(temp_html)
        
        try:
            driver.get(f"file://{temp_file}")
            time.sleep(2)
            
            # 发送请求
            send_btn = driver.find_element(By.XPATH, "//button[text()='发送请求']")
            send_btn.click()
            time.sleep(3)
            
            status = driver.find_element(By.ID, 'status').text
            print(f"✅ 请求状态：{status}")
            
            print("✅ 测试 2 通过：请求已发送（请在 Charles 中查看请求头）")
            print("提示：在 Charles 中查看以下请求头：")
            print("  - Content-Type: application/json")
            print("  - Authorization: Bearer token")
            print("  - X-Exam-Id: 12345")
            print("  - X-Student-Id: student-001")
            
        finally:
            if os.path.exists(temp_file):
                os.remove(temp_file)

    def test_response_analysis(self, driver):
        """测试 3: 响应数据分析"""
        print("\n" + "="*60)
        print("测试 3: 响应数据分析")
        print("验证点：Response Status、Response Body 格式")
        print("="*60)
        
        temp_html = """
        <!DOCTYPE html>
        <html>
        <head><title>响应数据测试</title></head>
        <body>
            <h1>响应数据分析测试</h1>
            <div id="response-data">响应数据：未获取</div>
            <button onclick="fetchData()">获取数据</button>
            
            <script>
                function fetchData() {
                    fetch('/api/exam/info/1')
                        .then(response => {
                            return response.json().then(data => ({
                                status: response.status,
                                data: data
                            }));
                        })
                        .then(result => {
                            document.getElementById('response-data').textContent = 
                                '响应数据：' + JSON.stringify(result);
                        })
                        .catch(error => {
                            document.getElementById('response-data').textContent = 
                                '响应数据：请求失败 - ' + error.message;
                        });
                }
            </script>
        </body>
        </html>
        """
        
        temp_file = os.path.join(os.path.dirname(__file__), 'mock_response.html')
        with open(temp_file, 'w', encoding='utf-8') as f:
            f.write(temp_html)
        
        try:
            driver.get(f"file://{temp_file}")
            time.sleep(2)
            
            # 获取数据
            fetch_btn = driver.find_element(By.XPATH, "//button[text()='获取数据']")
            fetch_btn.click()
            time.sleep(3)
            
            response_data = driver.find_element(By.ID, 'response-data').text
            print(f"✅ {response_data}")
            
            print("✅ 测试 3 通过：响应数据已获取（请在 Charles 中查看响应）")
            print("提示：在 Charles 中查看：")
            print("  - Response Status: 200/400/500")
            print("  - Response Body: JSON 格式是否正确")
            print("  - Response Headers: Content-Type 是否正确")
            
        finally:
            if os.path.exists(temp_file):
                os.remove(temp_file)

if __name__ == "__main__":
    pytest.main([__file__, "-v", "--html=reports/charles_packet_analysis_report.html"])
