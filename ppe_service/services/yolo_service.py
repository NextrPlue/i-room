import cv2
import time
import torch
import os
from ultralytics import YOLO
from services.ppe_check import check_violation
from services.violation_handler import handle_violation

# YOLO 추적 루프
MODEL_PATH = os.getenv("MODEL_PATH", "model/best_8m_v5.pt")
RTSP_URL = os.getenv("RTSP_URL", "test2.mp4")
device = "cuda" if torch.cuda.is_available() else "cpu"
model = YOLO(MODEL_PATH).to(device)

CLASS_NAMES = {0: "seatbelt_on", 1: "helmet_on"}


async def run_detection_loop(interval_sec: int = 10):
    cap = cv2.VideoCapture(RTSP_URL, cv2.CAP_FFMPEG)
    if not cap.isOpened():
        print("[ERROR] Cannot open video source")
        return

    print(f"[INFO] YOLO detection started → {RTSP_URL}")
    prev_time = time.time()
    helmet_votes, seatbelt_votes, total_frames = {}, {}, 0

    results = model.track(
        source=RTSP_URL,
        tracker="my_botsort.yaml",
        stream=True,
        device=device,
        persist=True,
        half=True,
        conf=0.3
    )

    for r in results:
        frame = r.orig_img
        if frame is None:
            continue
        total_frames += 1

        # 탐지 결과 집계
        for box in r.boxes:
            cls_id = int(box.cls[0])
            track_id = int(box.id[0]) if box.id is not None else -1
            if cls_id == 0:
                seatbelt_votes[track_id] = seatbelt_votes.get(track_id, 0) + 1
            elif cls_id == 1:
                helmet_votes[track_id] = helmet_votes.get(track_id, 0) + 1

        # interval마다 집계
        if time.time() - prev_time >= interval_sec:
            helmet_count = sum(1 for v in helmet_votes.values() if v / total_frames >= 0.5)
            seatbelt_count = sum(1 for v in seatbelt_votes.values() if v / total_frames >= 0.5)

            # 위반 여부 검사 (total_workers는 내부 DB 조회로 처리)
            check_result = check_violation(helmet_count, seatbelt_count)
            print(f"[CHECK] {check_result}")

            # 위반 발생 시 핸들러 호출
            if check_result["violation"]:
                handle_violation(helmet_count, seatbelt_count, frame, check_result)

            # 초기화
            helmet_votes.clear()
            seatbelt_votes.clear()
            total_frames = 0
            prev_time = time.time()