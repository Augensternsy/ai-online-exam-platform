import cv2
import os
import sys

def record_camera(output_path, duration=10, fps=30):
    cap = cv2.VideoCapture(0)
    
    if not cap.isOpened():
        print("错误：无法打开摄像头")
        return
    
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(output_path, fourcc, fps, (width, height))
    
    print(f"开始录制 {duration} 秒视频...")
    print(f"分辨率：{width}x{height}")
    print(f"帧率：{fps} fps")
    print("按 'q' 键可提前结束录制")
    
    frame_count = 0
    total_frames = duration * fps
    
    while frame_count < total_frames:
        ret, frame = cap.read()
        
        if not ret:
            print("错误：无法读取帧")
            break
        
        out.write(frame)
        
        cv2.imshow('Recording...', frame)
        
        if cv2.waitKey(1) & 0xFF == ord('q'):
            print("用户提前结束录制")
            break
        
        frame_count += 1
        
        if frame_count % fps == 0:
            print(f"已录制 {frame_count // fps} 秒...")
    
    cap.release()
    out.release()
    cv2.destroyAllWindows()
    
    print(f"\n视频已保存：{output_path}")
    print(f"总帧数：{frame_count}")
    print(f"实际时长：{frame_count / fps:.2f} 秒")

if __name__ == "__main__":
    output_dir = os.path.join(os.path.dirname(__file__), "..", "test_videos")
    os.makedirs(output_dir, exist_ok=True)
    
    if len(sys.argv) > 1:
        filename = sys.argv[1]
    else:
        filename = "test_exam.mp4"
    
    output_path = os.path.join(output_dir, filename)
    record_camera(output_path, duration=10, fps=30)
