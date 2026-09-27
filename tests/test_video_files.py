from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
import time
import os

def test_video_file(video_name, description):
    """测试视频文件是否能被 Chrome 正确加载"""
    print(f"\n{'='*60}")
    print(f"📹 测试场景：{description}")
    print(f"🎬 使用视频：{video_name}")
    print(f"{'='*60}")
    
    video_path = os.path.join(os.path.dirname(__file__), 'test_videos', video_name)
    
    if not os.path.exists(video_path):
        print(f"❌ 视频文件不存在：{video_path}")
        return False
    
    file_size = os.path.getsize(video_path)
    print(f"📁 视频文件大小：{file_size / 1024 / 1024:.2f} MB")
    
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
    try:
        driver = webdriver.Chrome(
            service=Service(ChromeDriverManager().install()),
            options=chrome_options
        )
    except Exception as e:
        print(f"❌ Chrome 启动失败: {str(e)}")
        return False
    
    try:
        driver.implicitly_wait(10)
        driver.maximize_window()
        
        print("✅ Chrome 浏览器启动成功")
        print(f"✅ 视频参数已注入：{video_name}")
        
        # 测试摄像头访问
        print("正在测试摄像头访问...")
        driver.get("about:blank")
        
        # 执行 JS 测试摄像头
        result = driver.execute_script("""
            return new Promise((resolve) => {
                navigator.mediaDevices.getUserMedia({ video: true, audio: false })
                    .then(stream => {
                        const track = stream.getVideoTracks()[0];
                        const settings = track.getSettings();
                        resolve({
                            success: true,
                            width: settings.width,
                            height: settings.height,
                            frameRate: settings.frameRate
                        });
                    })
                    .catch(err => {
                        resolve({
                            success: false,
                            error: err.message
                        });
                    });
            });
        """)
        
        time.sleep(2)
        
        if result and result.get('success'):
            print(f"✅ 摄像头访问成功")
            print(f"   分辨率：{result.get('width')}x{result.get('height')}")
            print(f"   帧率：{result.get('frameRate')} fps")
            print(f"✅ {description}测试通过")
            return True
        else:
            print(f"❌ 摄像头访问失败：{result.get('error', '未知错误')}")
            return False
        
    except Exception as e:
        print(f"❌ 测试失败: {str(e)}")
        return False
    finally:
        print("正在关闭浏览器...")
        driver.quit()
        print("✅ 浏览器已关闭")

if __name__ == "__main__":
    print("🚀 开始视频文件测试")
    print("="*60)
    
    test_cases = [
        ("no_person.mp4", "无人场景视频"),
        ("single_person.mp4", "单人场景视频"),
        ("two_persons.mp4", "双人场景视频"),
    ]
    
    results = {}
    
    for video_name, description in test_cases:
        try:
            result = test_video_file(video_name, description)
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
        print("🎉 所有视频文件测试通过！")
    else:
        print("⚠️  部分测试失败，请检查日志")
