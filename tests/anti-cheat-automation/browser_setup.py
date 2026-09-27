from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import os


class AntiCheatBrowser:
    
    def __init__(self, config=None):
        self.driver = None
        self.config = config or {}
        self.video_dir = os.path.join(os.path.dirname(__file__), "test_data", "videos")
        
    def create_driver_with_fake_video(self, video_file=None):
        options = Options()
        
        options.add_argument("--use-fake-device-for-media-stream")
        options.add_argument("--use-fake-ui-for-media-stream")
        
        if video_file:
            video_path = os.path.join(self.video_dir, video_file)
            if os.path.exists(video_path):
                options.add_argument(f"--use-file-for-fake-video-capture={video_path}")
                print(f"[INFO] 已挂载虚拟视频: {video_path}")
            else:
                print(f"[WARNING] 视频文件不存在: {video_path}，使用默认虚拟设备")
        
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        options.add_argument("--disable-gpu")
        options.add_argument("--window-size=1920,1080")
        
        if self.config.get("headless", False):
            options.add_argument("--headless")
        
        self.driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=options
        )
        
        self.driver.set_page_load_timeout(30)
        self.driver.implicitly_wait(10)
        
        return self.driver
    
    def create_normal_driver(self):
        options = Options()
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        options.add_argument("--window-size=1920,1080")
        
        if self.config.get("headless", False):
            options.add_argument("--headless")
        
        self.driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=options
        )
        
        self.driver.set_page_load_timeout(30)
        self.driver.implicitly_wait(10)
        
        return self.driver
    
    def quit(self):
        if self.driver:
            self.driver.quit()
            print("[INFO] 浏览器已关闭，缓存已清理")
