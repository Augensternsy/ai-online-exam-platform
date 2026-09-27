from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import time
import os

def test_video_injection(video_name, description):
    """测试视频注入功能"""
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
        
        # 测试视频注入
        print("正在测试视频注入...")
        driver.execute_script("""
            // 创建视频元素
            const video = document.createElement('video');
            video.autoplay = true;
            video.muted = true;
            
            // 获取摄像头流
            navigator.mediaDevices.getUserMedia({ video: true, audio: false })
                .then(stream => {
                    video.srcObject = stream;
                    document.body.appendChild(video);
                    console.log('视频流已注入');
                })
                .catch(err => {
                    console.error('视频流注入失败:', err);
                });
        """)
        
        time.sleep(3)
        
        # 检查视频元素
        video_exists = driver.execute_script("""
            return document.querySelector('video') !== null;
        """)
        
        if video_exists:
            print("✅ 视频元素创建成功")
            print(f"✅ {description}视频注入测试通过")
            return True
        else:
            print("❌ 视频元素创建失败")
            return False
        
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")
        return False
    finally:
        print("正在关闭浏览器...")
        driver.quit()
        print("✅ 浏览器已关闭")

if __name__ == "__main__":
    print("🚀 开始视频注入功能测试")
    print("="*60)
    
    test_cases = [
        ("no_person.mp4", "无人场景视频注入"),
        ("single_person.mp4", "单人场景视频注入"),
        ("two_persons.mp4", "双人场景视频注入"),
    ]
    
    results = {}
    
    for video_name, description in test_cases:
        try:
            result = test_video_injection(video_name, description)
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
        print("🎉 所有视频注入测试通过！")
    else:
        print("⚠️  部分测试失败，请检查日志")
