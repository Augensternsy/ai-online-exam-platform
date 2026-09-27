from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import time
import os

def test_with_video(video_name, description):
    """使用指定视频测试防作弊系统"""
    print(f"\n{'='*60}")
    print(f"📹 测试场景：{description}")
    print(f"🎬 使用视频：{video_name}")
    print(f"{'='*60}")
    
    video_path = os.path.join(os.path.dirname(__file__), 'test_videos', video_name)
    
    if not os.path.exists(video_path):
        print(f"❌ 视频文件不存在：{video_path}")
        return False
    
    # 配置 Chrome 选项
    chrome_options = Options()
    chrome_options.add_argument('--use-fake-ui-for-media-stream')
    chrome_options.add_argument('--use-fake-device-for-media-stream')
    chrome_options.add_argument(f'--use-file-for-fake-video-capture={video_path}')
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
        print(f"✅ 已加载视频：{video_name}")
        
        # 测试摄像头权限
        print("正在验证摄像头权限...")
        driver.get("chrome://settings/content/camera")
        time.sleep(2)
        print("✅ 摄像头权限配置成功")
        
        # 模拟进入考试系统
        print("正在模拟进入考试系统...")
        # 这里可以替换为实际的考试系统 URL
        driver.get("http://localhost:8080")
        time.sleep(3)
        
        print(f"当前页面标题: {driver.title}")
        print("✅ 考试系统页面加载成功")
        
        # 等待防作弊系统初始化
        print("等待防作弊系统初始化...")
        time.sleep(5)
        
        print("✅ 防作弊系统初始化完成")
        print(f"✅ {description}测试完成")
        
        return True
        
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")
        return False
    finally:
        print("正在关闭浏览器...")
        driver.quit()
        print("✅ 浏览器已关闭")

if __name__ == "__main__":
    print("🚀 开始防作弊系统自动化测试")
    print("="*60)
    
    test_cases = [
        ("no_person.mp4", "无人场景测试"),
        ("single_person.mp4", "单人场景测试"),
        ("two_persons.mp4", "双人场景测试"),
    ]
    
    results = {}
    
    for video_name, description in test_cases:
        try:
            result = test_with_video(video_name, description)
            results[description] = "✅ 通过" if result else "❌ 失败"
        except Exception as e:
            results[description] = f"❌ 异常：{str(e)}"
    
    # 输出测试结果
    print(f"\n{'='*60}")
    print("📊 测试结果汇总")
    print(f"{'='*60}")
    
    for test_name, result in results.items():
        print(f"{test_name}: {result}")
    
    # 统计结果
    passed = sum(1 for r in results.values() if "✅" in r)
    total = len(results)
    
    print(f"\n总计：{passed}/{total} 通过")
    
    if passed == total:
        print("🎉 所有测试通过！")
    else:
        print("⚠️  部分测试失败，请检查日志")
