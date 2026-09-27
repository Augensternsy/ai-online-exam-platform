from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import time
import os

def test_basic_functionality():
    """测试基本功能"""
    print("🚀 开始基本功能测试")
    
    # 配置 Chrome 选项
    chrome_options = Options()
    chrome_options.add_argument('--use-fake-ui-for-media-stream')
    chrome_options.add_argument('--use-fake-device-for-media-stream')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    chrome_options.add_argument('--disable-gpu')
    chrome_options.add_argument('--window-size=1920,1080')
    
    # 创建 WebDriver
    print("正在启动 Chrome 浏览器...")
    driver = webdriver.Chrome(
        service=Service(ChromeDriverManager().install()),
        options=chrome_options
    )
    
    try:
        driver.implicitly_wait(10)
        driver.maximize_window()
        
        print("✅ Chrome 浏览器启动成功")
        
        # 测试导航
        print("正在导航到百度...")
        driver.get("https://www.baidu.com")
        time.sleep(2)
        
        print(f"当前页面标题: {driver.title}")
        print("✅ 页面导航成功")
        
        # 测试摄像头权限
        print("正在测试摄像头权限...")
        driver.get("chrome://settings/content/camera")
        time.sleep(2)
        print("✅ 摄像头权限页面访问成功")
        
        return True
        
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")
        return False
    finally:
        print("正在关闭浏览器...")
        driver.quit()
        print("✅ 浏览器已关闭")

if __name__ == "__main__":
    result = test_basic_functionality()
    if result:
        print("\n🎉 基本功能测试通过！")
    else:
        print("\n❌ 基本功能测试失败！")
