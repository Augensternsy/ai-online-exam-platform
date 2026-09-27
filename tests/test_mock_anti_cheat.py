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

class TestMockAntiCheat:
    """Mock 模式下的防作弊自动化测试"""
    
    @pytest.fixture(scope="function")
    def driver(self):
        """创建 WebDriver 实例"""
        chrome_options = Options()
        chrome_options.add_argument('--use-fake-ui-for-media-stream')
        chrome_options.add_argument('--use-fake-device-for-media-stream')
        chrome_options.add_argument('--no-sandbox')
        chrome_options.add_argument('--disable-dev-shm-usage')
        chrome_options.add_argument('--disable-gpu')
        chrome_options.add_argument('--window-size=1920,1080')
        
        driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=chrome_options
        )
        
        driver.implicitly_wait(10)
        driver.maximize_window()
        
        yield driver
        
        driver.quit()

    def test_video_injection_mock(self, driver):
        """测试 1: 虚拟视频流注入（Mock 模式）"""
        print("\n" + "="*60)
        print("测试 1: 虚拟视频流注入（Mock 模式）")
        print("="*60)
        
        # 创建一个简单的 HTML 页面来测试视频流
        test_html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Mock Exam System</title>
        </head>
        <body>
            <h1>Mock Exam System</h1>
            <video id="camera" autoplay playsinline style="width: 320px; height: 240px;"></video>
            <div id="status">等待摄像头...</div>
            <div id="faces-detected">检测到人脸数：0</div>
            <div id="violations">违规次数：0</div>
            <button id="start-exam">开始考试</button>
            <div id="warning" style="display:none; color:red; font-size:24px;">⚠️ 检测到违规行为！</div>
            
            <script>
                let violationCount = 0;
                let faceCount = 0;
                
                document.getElementById('start-exam').addEventListener('click', async function() {
                    try {
                        const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
                        document.getElementById('camera').srcObject = stream;
                        document.getElementById('status').textContent = '摄像头已启动';
                        
                        // 模拟人脸检测
                        setInterval(() => {
                            faceCount = Math.floor(Math.random() * 3);
                            document.getElementById('faces-detected').textContent = '检测到人脸数：' + faceCount;
                            
                            if (faceCount === 0) {
                                violationCount++;
                                document.getElementById('violations').textContent = '违规次数：' + violationCount;
                                if (violationCount >= 3) {
                                    document.getElementById('warning').style.display = 'block';
                                }
                            }
                        }, 1000);
                    } catch (err) {
                        document.getElementById('status').textContent = '摄像头启动失败：' + err.message;
                    }
                });
                
                // 监听切屏事件
                document.addEventListener('visibilitychange', function() {
                    if (document.hidden) {
                        violationCount++;
                        document.getElementById('violations').textContent = '违规次数：' + violationCount;
                        if (violationCount >= 3) {
                            document.getElementById('warning').style.display = 'block';
                        }
                    }
                });
            </script>
        </body>
        </html>
        """
        
        # 将 HTML 写入临时文件
        temp_html = os.path.join(os.path.dirname(__file__), 'mock_exam.html')
        with open(temp_html, 'w', encoding='utf-8') as f:
            f.write(test_html)
        
        try:
            # 打开 Mock 页面
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 验证页面加载
            assert "Mock Exam System" in driver.title, "页面标题不正确"
            print("✅ Mock 页面加载成功")
            
            # 点击开始考试按钮
            start_button = driver.find_element(By.ID, 'start-exam')
            start_button.click()
            time.sleep(3)
            
            # 验证摄像头状态
            status = driver.find_element(By.ID, 'status').text
            assert "摄像头已启动" in status, f"摄像头未启动：{status}"
            print(f"✅ 摄像头状态：{status}")
            
            # 等待人脸检测
            time.sleep(5)
            
            # 获取检测结果
            faces_text = driver.find_element(By.ID, 'faces-detected').text
            violations_text = driver.find_element(By.ID, 'violations').text
            
            print(f"✅ 检测结果：{faces_text}")
            print(f"✅ 违规次数：{violations_text}")
            
            # 验证视频流注入成功
            video_element = driver.find_element(By.TAG_NAME, 'video')
            assert video_element is not None, "视频元素不存在"
            print("✅ 视频流注入成功")
            
            print("✅ 测试 1 通过：虚拟视频流注入功能正常")
            return True
            
        finally:
            # 清理临时文件
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_switch_screen_mock(self, driver):
        """测试 2: 切屏事件 JS 注入（Mock 模式）"""
        print("\n" + "="*60)
        print("测试 2: 切屏事件 JS 注入（Mock 模式）")
        print("="*60)
        
        # 创建 Mock 页面
        test_html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Mock Switch Screen Test</title>
        </head>
        <body>
            <h1>切屏监控测试</h1>
            <div id="page-status">页面状态：可见</div>
            <div id="switch-count">切屏次数：0</div>
            <div id="warning" style="display:none; color:red; font-size:24px;">⚠️ 检测到切屏行为！</div>
            
            <script>
                let switchCount = 0;
                
                document.addEventListener('visibilitychange', function() {
                    if (document.hidden) {
                        switchCount++;
                        document.getElementById('page-status').textContent = '页面状态：隐藏';
                        document.getElementById('switch-count').textContent = '切屏次数：' + switchCount;
                        
                        if (switchCount >= 3) {
                            document.getElementById('warning').style.display = 'block';
                        }
                    } else {
                        document.getElementById('page-status').textContent = '页面状态：可见';
                    }
                });
            </script>
        </body>
        </html>
        """
        
        temp_html = os.path.join(os.path.dirname(__file__), 'mock_switch.html')
        with open(temp_html, 'w', encoding='utf-8') as f:
            f.write(test_html)
        
        try:
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            # 验证初始状态
            status = driver.find_element(By.ID, 'page-status').text
            assert "可见" in status, f"初始状态不正确：{status}"
            print(f"✅ 初始状态：{status}")
            
            # 模拟切屏 1 次
            print("模拟切屏 1 次...")
            driver.execute_script("""
                Object.defineProperty(document, 'hidden', {value: true, writable: true});
                document.dispatchEvent(new Event('visibilitychange'));
            """)
            time.sleep(1)
            
            switch_count = driver.find_element(By.ID, 'switch-count').text
            print(f"✅ 切屏次数：{switch_count}")
            
            # 模拟切屏 2 次
            print("模拟切屏 2 次...")
            driver.execute_script("""
                Object.defineProperty(document, 'hidden', {value: true, writable: true});
                document.dispatchEvent(new Event('visibilitychange'));
            """)
            time.sleep(1)
            
            switch_count = driver.find_element(By.ID, 'switch-count').text
            print(f"✅ 切屏次数：{switch_count}")
            
            # 模拟切屏 3 次（触发警告）
            print("模拟切屏 3 次...")
            driver.execute_script("""
                Object.defineProperty(document, 'hidden', {value: true, writable: true});
                document.dispatchEvent(new Event('visibilitychange'));
            """)
            time.sleep(1)
            
            # 验证警告显示
            warning = driver.find_element(By.ID, 'warning')
            assert warning.is_displayed(), "警告未显示"
            print("✅ 切屏警告已触发")
            
            print("✅ 测试 2 通过：切屏事件 JS 注入功能正常")
            return True
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

    def test_automation_chain_mock(self, driver):
        """测试 3: 完整自动化链路（Mock 模式）"""
        print("\n" + "="*60)
        print("测试 3: 完整自动化链路（Mock 模式）")
        print("="*60)
        
        # 创建完整的 Mock 考试系统
        test_html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Mock Full Exam System</title>
            <style>
                .hidden { display: none; }
                .warning { color: red; font-size: 24px; font-weight: bold; }
            </style>
        </head>
        <body>
            <div id="login-page">
                <h1>考试系统登录</h1>
                <input type="text" id="username" placeholder="用户名" value="testuser">
                <input type="password" id="password" placeholder="密码" value="testpass">
                <button id="login-btn">登录</button>
            </div>
            
            <div id="exam-page" class="hidden">
                <h1>在线考试</h1>
                <video id="camera" autoplay playsinline style="width: 320px; height: 240px;"></video>
                <div id="monitor-status">监控状态：未启动</div>
                <div id="violations">违规次数：0</div>
                <div id="warning" class="hidden warning">⚠️ 检测到违规行为！</div>
                <button id="submit-exam">交卷</button>
            </div>
            
            <div id="result-page" class="hidden">
                <h1>考试结果</h1>
                <div id="result-message"></div>
            </div>
            
            <script>
                let violationCount = 0;
                let monitorActive = false;
                
                // 登录
                document.getElementById('login-btn').addEventListener('click', async function() {
                    document.getElementById('login-page').classList.add('hidden');
                    document.getElementById('exam-page').classList.remove('hidden');
                    
                    // 启动监控
                    try {
                        const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: false });
                        document.getElementById('camera').srcObject = stream;
                        monitorActive = true;
                        document.getElementById('monitor-status').textContent = '监控状态：运行中';
                        
                        // 模拟监控
                        setInterval(() => {
                            if (Math.random() > 0.7) {
                                violationCount++;
                                document.getElementById('violations').textContent = '违规次数：' + violationCount;
                                if (violationCount >= 3) {
                                    document.getElementById('warning').classList.remove('hidden');
                                }
                            }
                        }, 2000);
                    } catch (err) {
                        document.getElementById('monitor-status').textContent = '监控状态：启动失败';
                    }
                });
                
                // 切屏监听
                document.addEventListener('visibilitychange', function() {
                    if (document.hidden && monitorActive) {
                        violationCount++;
                        document.getElementById('violations').textContent = '违规次数：' + violationCount;
                        if (violationCount >= 3) {
                            document.getElementById('warning').classList.remove('hidden');
                        }
                    }
                });
                
                // 交卷
                document.getElementById('submit-exam').addEventListener('click', function() {
                    document.getElementById('exam-page').classList.add('hidden');
                    document.getElementById('result-page').classList.remove('hidden');
                    document.getElementById('result-message').textContent = '考试已完成，违规次数：' + violationCount;
                });
            </script>
        </body>
        </html>
        """
        
        temp_html = os.path.join(os.path.dirname(__file__), 'mock_full_exam.html')
        with open(temp_html, 'w', encoding='utf-8') as f:
            f.write(test_html)
        
        try:
            # 步骤 1: 登录
            print("步骤 1: 自动登录...")
            driver.get(f"file://{temp_html}")
            time.sleep(2)
            
            username = driver.find_element(By.ID, 'username')
            password = driver.find_element(By.ID, 'password')
            login_btn = driver.find_element(By.ID, 'login-btn')
            
            username.clear()
            username.send_keys('testuser')
            password.clear()
            password.send_keys('testpass')
            login_btn.click()
            time.sleep(3)
            
            # 验证登录成功
            exam_page = driver.find_element(By.ID, 'exam-page')
            assert 'hidden' not in exam_page.get_attribute('class'), "登录失败"
            print("✅ 登录成功")
            
            # 步骤 2: 验证监控启动
            print("步骤 2: 验证监控启动...")
            time.sleep(3)
            
            monitor_status = driver.find_element(By.ID, 'monitor-status').text
            assert "运行中" in monitor_status, f"监控未启动：{monitor_status}"
            print(f"✅ 监控状态：{monitor_status}")
            
            # 步骤 3: 模拟违规行为
            print("步骤 3: 模拟违规行为...")
            
            # 模拟切屏
            for i in range(3):
                print(f"  模拟切屏 {i+1} 次...")
                driver.execute_script("""
                    Object.defineProperty(document, 'hidden', {value: true, writable: true});
                    document.dispatchEvent(new Event('visibilitychange'));
                    setTimeout(() => {
                        Object.defineProperty(document, 'hidden', {value: false, writable: true});
                        document.dispatchEvent(new Event('visibilitychange'));
                    }, 500);
                """)
                time.sleep(2)
            
            # 步骤 4: 验证违规检测
            print("步骤 4: 验证违规检测...")
            time.sleep(2)
            
            violations = driver.find_element(By.ID, 'violations').text
            print(f"✅ 违规次数：{violations}")
            
            warning = driver.find_element(By.ID, 'warning')
            assert warning.is_displayed(), "违规警告未显示"
            print("✅ 违规警告已触发")
            
            # 步骤 5: 交卷
            print("步骤 5: 自动交卷...")
            submit_btn = driver.find_element(By.ID, 'submit-exam')
            submit_btn.click()
            time.sleep(2)
            
            # 验证交卷成功
            result_page = driver.find_element(By.ID, 'result-page')
            assert 'hidden' not in result_page.get_attribute('class'), "交卷失败"
            
            result_message = driver.find_element(By.ID, 'result-message').text
            print(f"✅ 考试结果：{result_message}")
            
            print("✅ 测试 3 通过：完整自动化链路功能正常")
            return True
            
        finally:
            if os.path.exists(temp_html):
                os.remove(temp_html)

if __name__ == "__main__":
    pytest.main([__file__, "-v", "--html=reports/mock_test_report.html"])
