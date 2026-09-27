import pytest
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import os
import time

@pytest.fixture(scope="session")
def driver():
    """创建 WebDriver 实例"""
    chrome_options = Options()
    
    # 测试环境配置
    chrome_options.add_argument('--use-fake-ui-for-media-stream')
    chrome_options.add_argument('--use-fake-device-for-media-stream')
    
    # 使用录制的 MP4 视频作为虚拟摄像头输入
    video_path = os.path.join(os.path.dirname(__file__), 'test_videos', 'no_person.mp4')
    chrome_options.add_argument(f'--use-file-for-fake-video-capture={video_path}')
    
    # 无头模式（可选）
    # chrome_options.add_argument('--headless')
    
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

@pytest.fixture(scope="function")
def login(driver):
    """登录 fixture"""
    from pages.login_page import LoginPage
    
    login_page = LoginPage(driver)
    login_page.login('testuser', 'testpassword')
    
    yield driver
    
    # 清理：退出登录
    login_page.logout()

@pytest.fixture(scope="function")
def exam_session(driver, login):
    """创建考试会话 fixture"""
    from pages.exam_page import ExamPage
    
    exam_page = ExamPage(driver)
    exam_page.start_exam(1)  # 使用试卷 ID 1
    
    yield exam_page
    
    # 清理：结束考试
    exam_page.end_exam()
