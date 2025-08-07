# -------------------- 1. 라이브러리 임포트 --------------------
import cv2
import time
import numpy as np
import os
import torch
from ultralytics import YOLO
from deep_sort_realtime.deepsort_tracker import DeepSort

# -------------------- 2. 설정 --------------------
# 모델과 영상 경로
MODEL_PATH = "./best.pt"
VIDEO_PATH = "./test_video.mp4"
OUTPUT_VIDEO_PATH = "./output_test_video.mp4"

# 클래스 정보 (YOLO 학습 시 정의한 순서에 맞게 수정)
CLASS_NAMES = {
    0: "seatbelt_on",
    1: "lanyard_on",
    2: "helmet_on",
}

# 클래스별 색상 정의 (선택)
CLASS_COLORS = {
    0: (0, 255, 0),       # seatbelt_on → 초록
    1: (255, 255, 0),     # lanyard_on → 노랑
    2: (255, 0, 255),     # helmet_on → 보라
}

CONF_THRESHOLD = 0.5  # 탐지 신뢰도 기준

# -------------------- 3. 모델 및 비디오 초기화 --------------------
model = YOLO(MODEL_PATH, task='detect') # task 명시
model.to("cpu") # 명시적으로 cpu 사용

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
use_gpu = False             # CPU 고정
tracker = DeepSort(
    max_age=5,
    embedder='mobilenet',   # mobilenet -> osnet_x0_25 -> osnet_x1_0
    half=False,             # GPU 없는 경우 half precision 사용 금지
    bgr=True,
    embedder_gpu=False      # CPU 환경에서는 반드시 False
)

# -------------------- 5. 유틸 함수: 박스 그리기 --------------------
def draw_box(frame, bbox, track_id, cls_id):
    label = CLASS_NAMES.get(cls_id, f'class_{cls_id}')
    color = CLASS_COLORS.get(cls_id, (255, 255, 255))  # 기본: 흰색
    x1, y1, x2, y2 = map(int, bbox)
    cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
    cv2.putText(frame, f'{label} ID:{track_id}', (x1, y1 - 10),
                cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

# -------------------- 6. 메인 처리 루프 --------------------
frame_count = 0
start_time = time.time()
SKIP_FRAME = 5
frame_index = 0

# 💡 프레임 스킵 대응용: 마지막 탐지 정보 저장 변수
last_tracked_objects = []         # (bbox, track_id, class_id) 리스트
last_tracked_frame_index = -1     # 마지막 탐지 프레임 번호

while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        print("✅ 영상 종료 또는 프레임 없음")
        break

    frame_index += 1

    if frame_index % SKIP_FRAME == 0:
        # --- 탐지 수행 ---
        results = model.predict(frame, conf=CONF_THRESHOLD, verbose=False)[0]
        detections = []
        for box, conf, cls in zip(results.boxes.xyxy, results.boxes.conf, results.boxes.cls):
            x1, y1, x2, y2 = map(int, box)
            detections.append([[x1, y1, x2 - x1, y2 - y1], float(conf), int(cls)])

        tracks = tracker.update_tracks(detections, frame=frame)

        # 추적 결과 저장
        last_tracked_objects = []
        for track in tracks:
            if not track.is_confirmed():
                continue
            tid = track.track_id
            bbox = track.to_ltrb()
            cls_id = int(track.det_class)
            last_tracked_objects.append((bbox, tid, cls_id))
            draw_box(frame, bbox, tid, cls_id)

        last_tracked_frame_index = frame_index
    else:
        # --- 이전 추적 결과 재사용 (보간) ---
        for bbox, tid, cls_id in last_tracked_objects:
            draw_box(frame, bbox, tid, cls_id)

    # FPS 표시
    frame_count += 1
    elapsed = time.time() - start_time
    fps_now = frame_count / elapsed
    cv2.putText(frame, f"FPS: {fps_now:.2f}", (20, 40),
                cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 0), 2)

    video_writer.write(frame)

# -------------------- 7. 종료 --------------------
cap.release()
video_writer.release()
cv2.destroyAllWindows()

print(f"🎬 탐지 완료. 결과 저장 위치: {OUTPUT_VIDEO_PATH}")
