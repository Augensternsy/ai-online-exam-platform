import os

def test_video_files():
    """测试视频文件是否存在且有效"""
    print("🚀 开始视频文件验证测试")
    print("="*60)
    
    test_videos = [
        ("no_person.mp4", "无人场景视频"),
        ("single_person.mp4", "单人场景视频"),
        ("two_persons.mp4", "双人场景视频"),
    ]
    
    results = {}
    
    for video_name, description in test_videos:
        print(f"\n📹 测试：{description}")
        print(f"🎬 文件：{video_name}")
        
        video_path = os.path.join(os.path.dirname(__file__), 'test_videos', video_name)
        
        # 检查文件是否存在
        if not os.path.exists(video_path):
            print(f"❌ 文件不存在：{video_path}")
            results[description] = "❌ 文件不存在"
            continue
        
        # 检查文件大小
        file_size = os.path.getsize(video_path)
        print(f"📁 文件大小：{file_size / 1024 / 1024:.2f} MB")
        
        if file_size == 0:
            print(f"❌ 文件为空")
            results[description] = "❌ 文件为空"
            continue
        
        # 检查文件扩展名
        if not video_name.endswith('.mp4'):
            print(f"❌ 文件格式不正确：{video_name}")
            results[description] = "❌ 格式错误"
            continue
        
        print(f"✅ 文件验证通过")
        results[description] = "✅ 通过"
    
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
        print("🎉 所有视频文件验证通过！")
        print("\n📋 视频文件清单：")
        for video_name, description in test_videos:
            video_path = os.path.join(os.path.dirname(__file__), 'test_videos', video_name)
            if os.path.exists(video_path):
                size = os.path.getsize(video_path) / 1024 / 1024
                print(f"  ✅ {video_name} - {size:.2f} MB")
    else:
        print("⚠️  部分测试失败，请检查日志")

if __name__ == "__main__":
    test_video_files()
