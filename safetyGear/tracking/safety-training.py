# -------------------- 1. 라이브러리 임포트 --------------------
import cv2
import time
import torch
import numpy as np
import os
from ultralytics import YOLO
from deep_sort_realtime.deepsort_tracker import DeepSort

# -------------------- 2. 설정 --------------------
MODEL_PATH = 'best_8n_somany.pt'          # 모델 경로
VIDEO_PATH = 'test2.mp4'        # 입력 비디오
OUTPUT_VIDEO_PATH = 'output_deepsort_cpu.mp4'  # 출력 비디오

CLASS_NAMES = {
    0: "seatbelt_on",
    1: "helmet_on",
}

CLASS_COLORS = {
    0: (0, 255, 0),       # seatbelt_on → 초록
    1: (0, 0, 255),       # lanyard_on → 빨강
    2: (255, 255, 0),     # helmet_on → 노랑
    3: (0, 255, 255),     # 추가 클래스 색상
    4: (255, 0, 255),
    5: (255, 0, 0),
}

CONF_THRESHOLD = 0.3  # confidence threshold

# -------------------- 3. 모델 및 비디오 초기화 --------------------
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"📌 Using device: {device}")

model = YOLO(MODEL_PATH).to(device)

cap = cv2.VideoCapture(VIDEO_PATH)
if not cap.isOpened():
    raise IOError("🚨 비디오를 열 수 없습니다. 경로를 확인하세요.")

width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
fps = cap.get(cv2.CAP_PROP_FPS)

video_writer = cv2.VideoWriter(
    OUTPUT_VIDEO_PATH,
    cv2.VideoWriter_fourcc(*'mp4v'),
    fps,
    (width, height)
)

# -------------------- 4. DeepSORT 추적기 초기화 --------------------
use_gpu = torch.cuda.is_available()
tracker = DeepSort(
    max_age=15,
    n_init=1,
    embedder='torchreid',   # torchreid를 사용
    half=True,
    bgr=True,
    embedder_gpu=use_gpu,
    model_name='osnet_x0_25'  # torchreid 모델명
)

# -------------------- 5. 유틸 함수 --------------------
def draw_box(frame, bbox, track_id, cls_id):
    label = CLASS_NAMES.get(cls_id, f'Class {cls_id}')
    color = CLASS_COLORS.get(cls_id, (255, 255, 255))
    x1, y1, x2, y2 = map(int, bbox)
    cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
    cv2.putText(frame, f"{label} ID:{track_id}", (x1, y1 - 10),
                cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

# -------------------- 6. 메인 처리 루프 --------------------
frame_count = 0
start_time = time.time()
track_class_map = {}  # track_id별 클래스 기록

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        print("✅ 영상 종료 또는 프레임 없음")
        break

    # YOLO 탐지
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

        # YOLO cls_id를 track_id에 매핑
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

    # 결과 저장 및 출력
    video_writer.write(frame)
    cv2.imshow('YOLOv8 + DeepSORT Tracking (Improved)', frame)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

# -------------------- 7. 종료 --------------------
cap.release()
video_writer.release()
cv2.destroyAllWindows()
print(f"🎬 탐지 완료. 결과 저장 위치: {OUTPUT_VIDEO_PATH}")
