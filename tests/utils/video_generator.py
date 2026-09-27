import numpy as np
import os
import struct

class VideoGenerator:
    @staticmethod
    def rgb_to_yuv(r, g, b):
        y = 0.299 * r + 0.587 * g + 0.114 * b
        u = -0.14713 * r - 0.28886 * g + 0.436 * b + 128
        v = 0.615 * r - 0.51499 * g - 0.10001 * b + 128
        return int(y), int(u), int(v)

    @staticmethod
    def write_y4m_header(file, width, height, fps):
        header = f"YUV4MPEG2 W{width} H{height} F{fps}:1 Ip A1:1 C420\n"
        file.write(header.encode('ascii'))

    @staticmethod
    def write_y4m_frame(file, frame_yuv):
        file.write(b"FRAME\n")
        file.write(frame_yuv)

    @staticmethod
    def generate_no_face_video(output_path, duration=10, fps=30):
        total_frames = duration * fps
        width, height = 640, 480
        
        with open(output_path, 'wb') as f:
            VideoGenerator.write_y4m_header(f, width, height, fps)
            
            for _ in range(total_frames):
                y_plane = np.full((height, width), 16, dtype=np.uint8)
                u_plane = np.full((height // 2, width // 2), 128, dtype=np.uint8)
                v_plane = np.full((height // 2, width // 2), 128, dtype=np.uint8)
                
                frame_yuv = y_plane.tobytes() + u_plane.tobytes() + v_plane.tobytes()
                VideoGenerator.write_y4m_frame(f, frame_yuv)
        
        print(f"无人视频已生成: {output_path}")

    @staticmethod
    def generate_multiple_faces_video(output_path, duration=10, fps=30, face_count=3):
        total_frames = duration * fps
        width, height = 640, 480
        
        face_positions = []
        spacing = width // (face_count + 1)
        for i in range(face_count):
            x = spacing * (i + 1)
            y = height // 2
            face_positions.append((x, y))
        
        with open(output_path, 'wb') as f:
            VideoGenerator.write_y4m_header(f, width, height, fps)
            
            for frame_idx in range(total_frames):
                y_plane = np.full((height, width), 16, dtype=np.uint8)
                u_plane = np.full((height // 2, width // 2), 128, dtype=np.uint8)
                v_plane = np.full((height // 2, width // 2), 128, dtype=np.uint8)
                
                for x, y in face_positions:
                    offset = int(10 * np.sin(frame_idx * 0.1))
                    fx, fy = x + offset, y
                    
                    for dy in range(-50, 51):
                        for dx in range(-50, 51):
                            if dx*dx + dy*dy <= 2500:
                                px, py = fx + dx, fy + dy
                                if 0 <= px < width and 0 <= py < height:
                                    y_plane[py, px] = 235
                                    if py % 2 == 0 and px % 2 == 0:
                                        u_plane[py // 2, px // 2] = 128
                                        v_plane[py // 2, px // 2] = 128
                
                frame_yuv = y_plane.tobytes() + u_plane.tobytes() + v_plane.tobytes()
                VideoGenerator.write_y4m_frame(f, frame_yuv)
        
        print(f"多人视频已生成: {output_path}")

    @staticmethod
    def generate_single_face_video(output_path, duration=10, fps=30):
        total_frames = duration * fps
        width, height = 640, 480
        
        with open(output_path, 'wb') as f:
            VideoGenerator.write_y4m_header(f, width, height, fps)
            
            for frame_idx in range(total_frames):
                y_plane = np.full((height, width), 16, dtype=np.uint8)
                u_plane = np.full((height // 2, width // 2), 128, dtype=np.uint8)
                v_plane = np.full((height // 2, width // 2), 128, dtype=np.uint8)
                
                offset_x = int(20 * np.sin(frame_idx * 0.05))
                offset_y = int(10 * np.sin(frame_idx * 0.03))
                
                cx, cy = width // 2 + offset_x, height // 2 + offset_y
                
                for dy in range(-80, 81):
                    for dx in range(-80, 81):
                        if dx*dx + dy*dy <= 6400:
                            px, py = cx + dx, cy + dy
                            if 0 <= px < width and 0 <= py < height:
                                y_plane[py, px] = 235
                                if py % 2 == 0 and px % 2 == 0:
                                    u_plane[py // 2, px // 2] = 128
                                    v_plane[py // 2, px // 2] = 128
                
                frame_yuv = y_plane.tobytes() + u_plane.tobytes() + v_plane.tobytes()
                VideoGenerator.write_y4m_frame(f, frame_yuv)
        
        print(f"单人视频已生成: {output_path}")

if __name__ == "__main__":
    output_dir = os.path.join(os.path.dirname(__file__), "..", "test_videos")
    os.makedirs(output_dir, exist_ok=True)
    
    VideoGenerator.generate_no_face_video(os.path.join(output_dir, "no_face.y4m"))
    VideoGenerator.generate_multiple_faces_video(os.path.join(output_dir, "multiple_faces.y4m"))
    VideoGenerator.generate_single_face_video(os.path.join(output_dir, "single_face.y4m"))
