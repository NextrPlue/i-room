# -------------------- 1. 라이브러리 임포트 --------------------
import cv2
import time
import torch
from ultralytics import YOLO
from deep_sort_realtime.deepsort_tracker import DeepSort

# -------------------- 2. 설정 --------------------
MODEL_PATH = "model/best_8n_finished_int8.engine"  # TensorRT INT8 엔진 파일
VIDEO_PATH = "test2.mp4"
OUTPUT_VIDEO_PATH = "output_deepsort_trt.mp4"

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

CONF_THRESHOLD = 0.35
TARGET_SIZE = (640, 640)

# -------------------- 3. 모델 초기화 --------------------
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"📌 Using device: {device}")

# TensorRT INT8 YOLO 모델 로드
model = YOLO(MODEL_PATH)

# -------------------- 4. 비디오 초기화 --------------------
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
    (width, height)
)

# -------------------- 5. DeepSORT 추적기 초기화 --------------------
use_gpu = torch.cuda.is_available()
tracker = DeepSort(
    max_age=15,
    n_init=2,
    embedder='mobilenet',
    half=True,
    bgr=True,
    embedder_gpu=use_gpu
)

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
        print("✅ 영상 종료 또는 프레임 없음")
        break

    frame = cv2.resize(frame, TARGET_SIZE)

    # YOLO TensorRT 추론
    results = model.predict(frame, conf=CONF_THRESHOLD, device=device, verbose=False)[0]

    detections, cls_list = [], []
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

    # FPS 표시
    frame_count += 1
    elapsed = time.time() - start_time
    fps_now = frame_count / elapsed
    cv2.putText(frame, f"FPS: {fps_now:.2f}", (20, 40),
                cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 0), 2)

    video_writer.write(frame)
    cv2.imshow('YOLOv8 TRT INT8 + DeepSORT', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
video_writer.release()
cv2.destroyAllWindows()
