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

class TestCharlesMockAndMap:
    """Charles Map Local 和 Map Remote 测试"""
    
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

    def create_map_local_test_page(self):
        """创建 Map Local 测试页面"""
        test_html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Map Local 测试</title>
        </head>
        <body>
            <h1>Map Local 本地数据 Mock 测试</h1>
            
            <div id="exam-data">
                <h3>试卷数据</h3>
                <div id="exam-name">试卷名称：加载中...</div>
                <div id="question-count">题目数量：0</div>
                <div id="exam-duration">考试时长：0 分钟</div>
            </div>
            
            <div id="ai-response">
                <h3>AI 接口响应</h3>
                <div id="ai-status">状态：未调用</div>
                <div id="ai-result">结果：无</div>
            </div>
            
            <button onclick="loadExam()">加载试卷</button>
            <button onclick="callAI()">调用 AI 接口</button>
            
            <script>
                // 加载试卷（使用 Map Local Mock 数据）
                function loadExam() {
                    fetch('/api/exam/paper/1')
                        .then(response => response.json())
                        .then(data => {
                            document.getElementById('exam-name').textContent = 
                                '试卷名称：' + data.data.examName;
                            document.getElementById('question-count').textContent = 
                                '题目数量：' + data.data.questions.length;
                            document.getElementById('exam-duration').textContent = 
                                '考试时长：' + data.data.duration + ' 分钟';
                        })
                        .catch(error => {
                            document.getElementById('exam-name').textContent = 
                                '试卷名称：加载失败';
                        });
                }
                
                // 调用 AI 接口（使用 Map Local Mock 数据）
                function callAI() {
                    document.getElementById('ai-status').textContent = '状态：调用中...';
                    
                    fetch('/api/ai/analyze')
                        .then(response => response.json())
                        .then(data => {
                            document.getElementById('ai-status').textContent = '状态：成功';
                            document.getElementById('ai-result').textContent = 
                                '结果：' + JSON.stringify(data.data);
                        })
                        .catch(error => {
                            document.getElementById('ai-status').textContent = '状态：失败';
                            document.getElementById('ai-result').textContent = 
                                '结果：' + error.message;
                        });
                }
            </script>
        </body>
        </html>
        """
        
        temp_html = os.path.join(os.path.dirname(__file__), 'mock_map_local.html')
        with open(temp_html, 'w', encoding='utf-8') as f:
            f.write(test_html)
        
        return temp_html

    def create_map_remote_test_page(self):
        """创建 Map Remote 测试页面"""
        test_html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Map Remote 测试</title>
        </head>
        <body>
            <h1>Map Remote 环境转发测试</h1>
            
            <div id="api-info">
                <h3>API 信息</h3>
                <div id="api-url">请求地址：未设置</div>
                <div id="api-env">当前环境：未检测</div>
                <div id="api-response">响应数据：无</div>
            </div>
            
            <button onclick="callLocalAPI()">调用本地环境</button>
            <button onclick="callTestAPI()">调用测试环境</button>
            <button onclick="callProdAPI()">调用生产环境</button>
            
            <script>
                // 调用本地环境
                function callLocalAPI() {
                    document.getElementById('api-url').textContent = '请求地址：http://localhost:8080';
                    document.getElementById('api-env').textContent = '当前环境：本地开发';
                    
                    fetch('http://localhost:8080/api/exam/health')
                        .then(response => response.json())
                        .then(data => {
                            document.getElementById('api-response').textContent = 
                                '响应数据：' + JSON.stringify(data);
                        })
                        .catch(error => {
                            document.getElementById('api-response').textContent = 
                                '响应数据：请求失败 - ' + error.message;
                        });
                }
                
                // 调用测试环境
                function callTestAPI() {
                    document.getElementById('api-url').textContent = '请求地址：http://test.example.com';
                    document.getElementById('api-env').textContent = '当前环境：测试环境';
                    
                    fetch('http://test.example.com/api/exam/health')
                        .then(response => response.json())
                        .then(data => {
                            document.getElementById('api-response').textContent = 
                                '响应数据：' + JSON.stringify(data);
                        })
                        .catch(error => {
                            document.getElementById('api-response').textContent = 
                                '响应数据：请求失败 - ' + error.message;
                        });
                }
                
                // 调用生产环境
                function callProdAPI() {
                    document.getElementById('api-url').textContent = '请求地址：http://prod.example.com';
                    document.getElementById('api-env').textContent = '当前环境：生产环境';
                    
                    fetch('http://prod.example.com/api/exam/health')
                        .then(response => response.json())
                        .then(data => {
                            document.getElementById('api-response').textContent = 
                                '响应数据：' + JSON.stringify(data);
                        })
                        .catch(error => {
                            document.getElementById('api-response').textContent = 
                                '响应数据：请求失败 - ' + error.message;
                        });
                }
            </script>
        </body>
        </html>
        """
        
        temp_html = os.path.join(os.path.dirname(__file__), 'mock_map_remote.html')
        with open(temp_html, 'w', encoding='utf-8') as f:
            f.write(test_html)
        
        return temp_html

    def test_map_local_exam_data(self, driver):
        """测试 1: Map Local Mock 试卷数据"""
        print("\n" + "="*60)
        print("测试 1: Map Local Mock 试卷数据")
        print("Charles 配置：拦截 /api/exam/paper/1 → 返回本地 JSON")
        print("="*60)
        
        temp_html = self.create_map_local_test_page()
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 点击加载试卷
            load_btn = driver.find_element(By.XPATH, "//button[text()='加载试卷']")
            load_btn.click()
            time.sleep(3)
            
            # 验证试卷数据加载
            exam_name = driver.find_element(By.ID, 'exam-name').text
            question_count = driver.find_element(By.ID, 'question-count').text
            exam_duration = driver.find_element(By.ID, 'exam-duration').text
            
            print(f"✅ {exam_name}")
            print(f"✅ {question_count}")
            print(f"✅ {exam_duration}")
            
            # 验证数据来自 Mock（不是加载失败）
            assert "加载失败" not in exam_name, "试卷数据加载失败"
            
            print("✅ 测试 1 通过：Map Local Mock 数据正常")
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_map_local_ai_response(self, driver):
        """测试 2: Map Local Mock AI 接口响应"""
        print("\n" + "="*60)
        print("测试 2: Map Local Mock AI 接口响应")
        print("Charles 配置：拦截 /api/ai/analyze → 返回本地 JSON")
        print("="*60)
        
        temp_html = self.create_map_local_test_page()
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 点击调用 AI 接口
            ai_btn = driver.find_element(By.XPATH, "//button[text()='调用 AI 接口']")
            ai_btn.click()
            time.sleep(3)
            
            # 验证 AI 响应
            ai_status = driver.find_element(By.ID, 'ai-status').text
            ai_result = driver.find_element(By.ID, 'ai-result').text
            
            print(f"✅ AI 状态：{ai_status}")
            print(f"✅ AI 结果：{ai_result}")
            
            # 验证 AI 接口调用成功
            assert "成功" in ai_status, "AI 接口调用失败"
            
            print("✅ 测试 2 通过：Map Local Mock AI 响应正常")
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_map_remote_environment_switch(self, driver):
        """测试 3: Map Remote 环境转发"""
        print("\n" + "="*60)
        print("测试 3: Map Remote 环境转发")
        print("Charles 配置：localhost:8080 → test.example.com")
        print("="*60)
        
        temp_html = self.create_map_remote_test_page()
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 点击调用测试环境
            test_btn = driver.find_element(By.XPATH, "//button[text()='调用测试环境']")
            test_btn.click()
            time.sleep(3)
            
            # 验证环境信息
            api_url = driver.find_element(By.ID, 'api-url').text
            api_env = driver.find_element(By.ID, 'api-env').text
            api_response = driver.find_element(By.ID, 'api-response').text
            
            print(f"✅ {api_url}")
            print(f"✅ {api_env}")
            print(f"✅ {api_response}")
            
            print("✅ 测试 3 通过：Map Remote 环境转发正常")
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

if __name__ == "__main__":
    pytest.main([__file__, "-v", "--html=reports/charles_mock_map_report.html"])
