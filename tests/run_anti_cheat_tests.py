import pytest
import os
import sys
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

def run_test_with_video(video_name, test_function):
    """使用指定视频运行测试"""
    video_path = os.path.join(os.path.dirname(__file__), 'test_videos', video_name)
    
    if not os.path.exists(video_path):
        print(f"错误：视频文件不存在 {video_path}")
        return False
    
    print(f"\n{'='*60}")
    print(f"开始测试：{video_name}")
    print(f"视频路径：{video_path}")
    print(f"{'='*60}\n")
    
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
    driver = webdriver.Chrome(
        service=Service(ChromeDriverManager().install()),
        options=chrome_options
    )
    
    try:
        driver.implicitly_wait(10)
        driver.maximize_window()
        
        # 运行测试函数
        result = test_function(driver)
        
        return result
    except Exception as e:
        print(f"测试失败：{str(e)}")
        return False
    finally:
        driver.quit()

def test_no_person_scenario(driver):
    """测试无人场景"""
    from pages.login_page import LoginPage
    from pages.exam_page import ExamPage
    
    print("步骤 1: 登录系统")
    login_page = LoginPage(driver)
    login_page.login('testuser', 'testpassword')
    
    if not login_page.is_login_success():
        print("❌ 登录失败")
        return False
    
    print("✅ 登录成功")
    
    print("步骤 2: 进入考试")
    exam_page = ExamPage(driver)
    exam_page.start_exam(1)
    
    import time
    time.sleep(5)
    
    print("步骤 3: 等待防作弊系统检测")
    time.sleep(10)
    
    # 检查是否检测到无人场景
    if exam_page.is_violation_warning_shown():
        print("✅ 成功检测到无人场景")
        return True
    else:
        print("❌ 未检测到无人场景")
        return False

def test_single_person_scenario(driver):
    """测试单人场景"""
    from pages.login_page import LoginPage
    from pages.exam_page import ExamPage
    
    print("步骤 1: 登录系统")
    login_page = LoginPage(driver)
    login_page.login('testuser', 'testpassword')
    
    if not login_page.is_login_success():
        print("❌ 登录失败")
        return False
    
    print("✅ 登录成功")
    
    print("步骤 2: 进入考试")
    exam_page = ExamPage(driver)
    exam_page.start_exam(1)
    
    import time
    time.sleep(5)
    
    print("步骤 3: 等待防作弊系统检测")
    time.sleep(10)
    
    # 检查是否正常（不应该有违规警告）
    if not exam_page.is_violation_warning_shown():
        print("✅ 单人场景正常，无违规警告")
        return True
    else:
        print("❌ 单人场景出现意外警告")
        return False

def test_two_persons_scenario(driver):
    """测试双人场景"""
    from pages.login_page import LoginPage
    from pages.exam_page import ExamPage
    
    print("步骤 1: 登录系统")
    login_page = LoginPage(driver)
    login_page.login('testuser', 'testpassword')
    
    if not login_page.is_login_success():
        print("❌ 登录失败")
        return False
    
    print("✅ 登录成功")
    
    print("步骤 2: 进入考试")
    exam_page = ExamPage(driver)
    exam_page.start_exam(1)
    
    import time
    time.sleep(5)
    
    print("步骤 3: 等待防作弊系统检测")
    time.sleep(10)
    
    # 检查是否检测到多人场景
    if exam_page.is_violation_warning_shown():
        print("✅ 成功检测到双人场景")
        return True
    else:
        print("❌ 未检测到双人场景")
        return False

if __name__ == "__main__":
    print("🚀 开始自动化防作弊测试")
    print("="*60)
    
    test_cases = [
        ("no_person.mp4", "无人场景测试", test_no_person_scenario),
        ("single_person.mp4", "单人场景测试", test_single_person_scenario),
        ("two_persons.mp4", "双人场景测试", test_two_persons_scenario),
    ]
    
    results = {}
    
    for video_name, test_name, test_func in test_cases:
        print(f"\n{'='*60}")
        print(f"📹 运行测试：{test_name}")
        print(f"🎬 使用视频：{video_name}")
        print(f"{'='*60}")
        
        try:
            result = run_test_with_video(video_name, test_func)
            results[test_name] = "✅ 通过" if result else "❌ 失败"
        except Exception as e:
            results[test_name] = f"❌ 异常：{str(e)}"
    
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
