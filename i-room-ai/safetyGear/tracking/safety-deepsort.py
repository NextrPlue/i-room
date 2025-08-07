# -------------------- 1. 라이브러리 임포트 --------------------
import cv2
import time
import torch
import psutil
import logging
from ultralytics import YOLO
from deep_sort_realtime.deepsort_tracker import DeepSort

# -------------------- 2. 설정 --------------------
MODEL_PATH = '../model/best_8m_v4.pt'
VIDEO_PATH = "test2.mp4"
OUTPUT_VIDEO_PATH = "output_deepsort.mp4"
LOG_PATH = "deepsort_inference_log.txt"

CLASS_NAMES = {
    0: "seatbelt_on",
    1: "helmet_on",
}

CLASS_COLORS = {
    0: (0, 255, 0),
    1: (0, 0, 255),
    2: (255, 255, 0),
    3: (0, 255, 255),
    4: (255, 0, 255),
    5: (255, 0, 0),
}

CONF_THRESHOLD = 0.4
TARGET_SIZE = (640, 640)
LOG_INTERVAL = 30  # 30프레임마다 로그 저장

# -------------------- 3. 모델 및 비디오 초기화 --------------------
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Using device: {device}")

model = YOLO(MODEL_PATH).to(device)

cap = cv2.VideoCapture(VIDEO_PATH)
if not cap.isOpened():
    raise IOError("비디오를 열 수 없습니다. 경로를 확인하세요.")

width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
fps = cap.get(cv2.CAP_PROP_FPS)

video_writer = cv2.VideoWriter(
    OUTPUT_VIDEO_PATH,
    cv2.VideoWriter_fourcc(*'mp4v'),
    fps,
    TARGET_SIZE
)

# -------------------- 4. DeepSORT 추적기 초기화 --------------------
use_gpu = torch.cuda.is_available()
tracker = DeepSort(
    max_age=30,
    n_init=3,
    max_cosine_distance=0.2,  # 기본보다 살짝 더 엄격하게
    nn_budget=150,
    embedder="mobilenet",
    embedder_gpu=True,
    half=True,
    bgr=True
)

# -------------------- 5. 로그 초기화 --------------------
logging.basicConfig(filename=LOG_PATH, level=logging.INFO)
process = psutil.Process()

# -------------------- 6. 유틸 함수 --------------------
def draw_box(frame, bbox, track_id, cls_id):
    label = CLASS_NAMES.get(cls_id, f'class_{cls_id}')
    color = CLASS_COLORS.get(cls_id, (255, 255, 255))
    x1, y1, x2, y2 = map(int, bbox)
    cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
    cv2.putText(frame, f'{label} ID:{track_id}', (x1, y1 - 10),
                cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

# -------------------- 7. 메인 처리 루프 --------------------
frame_count = 0
start_time = time.time()
track_class_map = {}

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        print("영상 종료 또는 프레임 없음")
        break

    frame = cv2.resize(frame, TARGET_SIZE)
    detections, cls_list = [], []

    # YOLO 탐지 (매 프레임 실행)
    results = model.predict(frame, conf=CONF_THRESHOLD, device=device, verbose=False)[0]
    for box, conf, cls in zip(results.boxes.xyxy, results.boxes.conf, results.boxes.cls):
        x1, y1, x2, y2 = map(int, box)
        detections.append([[x1, y1, x2 - x1, y2 - y1], float(conf), int(cls)])
        cls_list.append(int(cls))

    # DeepSORT 추적
    tracks = tracker.update_tracks(detections, frame=frame)

    for i, track in enumerate(tracks):
        if not track.is_confirmed():
            continue
        tid = track.track_id
        bbox = track.to_ltrb()

        if tid not in track_class_map and i < len(cls_list):
            track_class_map[tid] = cls_list[i]

        cls_id = track_class_map.get(tid, 0)
        draw_box(frame, bbox, tid, cls_id)

    # FPS 계산 및 표시
    frame_count += 1
    elapsed = time.time() - start_time
    fps_now = frame_count / elapsed
    cv2.putText(frame, f"FPS: {fps_now:.2f}", (20, 40),
                cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 0), 2)

    # -------------------- 로그 기록 --------------------
    if frame_count % LOG_INTERVAL == 0:
        gpu_mem = torch.cuda.memory_allocated() / (1024 * 1024) if torch.cuda.is_available() else 0
        cpu_mem = process.memory_info().rss / (1024 * 1024)
        logging.info(f"Frame {frame_count} | FPS: {fps_now:.2f} | "
                     f"GPU: {gpu_mem:.2f} MB | CPU: {cpu_mem:.2f} MB")

    # 결과 저장 및 출력
    video_writer.write(frame)
    cv2.imshow('YOLOv8 + DeepSORT Tracking', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
video_writer.release()
cv2.destroyAllWindows()
print(f"결과 저장: {OUTPUT_VIDEO_PATH}")
print(f"로그 저장: {LOG_PATH}")
