import cv2
import numpy as np
import os


def create_empty_room_video(output_path, duration=5, fps=10):
    width, height = 640, 480
    fourcc = cv2.VideoWriter_fourcc(*'YUY2')
    out = cv2.VideoWriter(output_path, fourcc, fps, (width, height))
    
    for _ in range(duration * fps):
        frame = np.zeros((height, width, 3), dtype=np.uint8)
        frame[:, :] = [200, 200, 200]
        
        cv2.putText(frame, "Empty Room - No Face", (150, 240),
                   cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 0, 255), 2)
        
        out.write(frame)
    
    out.release()
    print(f"[INFO] 已生成空房间视频: {output_path}")


def create_multiple_faces_video(output_path, duration=5, fps=10):
    width, height = 640, 480
    fourcc = cv2.VideoWriter_fourcc(*'YUY2')
    out = cv2.VideoWriter(output_path, fourcc, fps, (width, height))
    
    for i in range(duration * fps):
        frame = np.zeros((height, width, 3), dtype=np.uint8)
        frame[:, :] = [200, 200, 200]
        
        cv2.circle(frame, (200, 200), 50, (0, 255, 0), -1)
        cv2.putText(frame, "Face 1", (170, 205),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 0), 2)
        
        cv2.circle(frame, (440, 200), 50, (0, 255, 0), -1)
        cv2.putText(frame, "Face 2", (410, 205),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 0), 2)
        
        cv2.putText(frame, "Multiple Faces Detected", (120, 400),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2)
        
        out.write(frame)
    
    out.release()
    print(f"[INFO] 已生成多人脸视频: {output_path}")


def create_single_face_video(output_path, duration=5, fps=10):
    width, height = 640, 480
    fourcc = cv2.VideoWriter_fourcc(*'YUY2')
    out = cv2.VideoWriter(output_path, fourcc, fps, (width, height))
    
    for i in range(duration * fps):
        frame = np.zeros((height, width, 3), dtype=np.uint8)
        frame[:, :] = [200, 200, 200]
        
        offset_x = int(10 * np.sin(i * 0.1))
        offset_y = int(5 * np.cos(i * 0.1))
        
        cv2.circle(frame, (320 + offset_x, 240 + offset_y), 60, (0, 255, 0), -1)
        cv2.putText(frame, "Single Face", (280, 245),
                   cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 0, 0), 2)
        
        out.write(frame)
    
    out.release()
    print(f"[INFO] 已生成单人脸视频: {output_path}")


if __name__ == "__main__":
    video_dir = os.path.join(os.path.dirname(__file__), "test_data", "videos")
    os.makedirs(video_dir, exist_ok=True)
    
    create_empty_room_video(os.path.join(video_dir, "no_face.y4m"))
    create_multiple_faces_video(os.path.join(video_dir, "multiple_faces.y4m"))
    create_single_face_video(os.path.join(video_dir, "single_face.y4m"))
    
    print("[INFO] 所有测试视频生成完成")
