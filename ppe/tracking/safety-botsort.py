import cv2
import torch
import time
import psutil
import logging
from ultralytics import YOLO

#설정 
MODEL_PATH = 'model/best_8m_v4.pt'
VIDEO_PATH = "test2.mp4"
OUTPUT_VIDEO_PATH = "output_bot_sort_test.mp4"
LOG_PATH = "botsort_inference_log.txt"

CLASS_NAMES = {
    0: "seatbelt_on",
    1: "helmet_on",
}

# 장치 설정 
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Using device: {device}")

# FP16 모드 적용 (CUDA 전용)
if device == "cuda":
    model = YOLO(MODEL_PATH).to(device).half()
    print("FP16(Half precision) 모드 활성화")
else:
    model = YOLO(MODEL_PATH).to(device)
    print("CPU 모드: FP16 미지원, FP32로 실행")

# -------------------- 로그 설정 --------------------
logging.basicConfig(filename=LOG_PATH, level=logging.INFO)
process = psutil.Process()

# -------------------- BoT-SORT 추적 시작 --------------------
results = model.track(
    source=VIDEO_PATH,
    tracker="my_botsort.yaml",
    stream=True,
    device=device,
    persist=True,
    half=(device == "cuda")  # FP16 적용 여부
)

# -------------------- 비디오 저장 준비 --------------------
cap = cv2.VideoCapture(VIDEO_PATH)
fps_video = cap.get(cv2.CAP_PROP_FPS)
video_writer = cv2.VideoWriter(
    OUTPUT_VIDEO_PATH,
    cv2.VideoWriter_fourcc(*'mp4v'),
    fps_video,
    (640, 640)
)

frame_count = 0
start_time = time.time()

for r in results:
    frame = r.orig_img.copy()
    frame_count += 1

    orig_h, orig_w = frame.shape[:2]
    target_w, target_h = 640, 640
    frame = cv2.resize(frame, (target_w, target_h))

    # FPS 계산
    elapsed = time.time() - start_time
    fps_now = frame_count / elapsed

    # 화면에 FPS 표시
    cv2.putText(frame, f"FPS: {fps_now:.2f}", (20, 40),
                cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 255), 2)

    # 바운딩 박스 표시
    for box in r.boxes:
        x1, y1, x2, y2 = map(float, box.xyxy[0])
        scale_x = target_w / orig_w
        scale_y = target_h / orig_h
        x1, y1, x2, y2 = int(x1 * scale_x), int(y1 * scale_y), int(x2 * scale_x), int(y2 * scale_y)

        cls_id = int(box.cls[0])
        conf = float(box.conf[0])
        label = f"{CLASS_NAMES.get(cls_id, str(cls_id))} {conf:.2f}"
        color = (0, 255, 0)

        cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
        cv2.putText(frame, label, (x1, y1 - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

    # -------------------- 로그 기록 (30프레임마다) --------------------
    if frame_count % 30 == 0:
        gpu_mem = torch.cuda.memory_allocated() / (1024 * 1024) if torch.cuda.is_available() else 0
        cpu_mem = process.memory_info().rss / (1024 * 1024)
        logging.info(f"Frame {frame_count} | FPS: {fps_now:.2f} | "
                     f"GPU: {gpu_mem:.2f} MB | CPU: {cpu_mem:.2f} MB")

    # 영상 출력 및 저장
    cv2.imshow("YOLOv8 + BoT-SORT (640x640, FP16)", frame)
    video_writer.write(frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
video_writer.release()
cv2.destroyAllWindows()
print(f"결과 저장: {OUTPUT_VIDEO_PATH}")
print(f"로그 저장: {LOG_PATH}")
