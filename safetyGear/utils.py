# safetyGear/utils.py
from ultralytics import YOLO
import cv2
import os

# 현재 파일 기준 경로로 모델 찾기
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "model", "best_8s_many.pt")

model = YOLO(MODEL_PATH)

# 탐지해야 하는 객체들의 클래스 이름
classes_name = {
    0: "safety_harness_on",
    1: "safety_lanyard_on",
    2: "safety_helmet_on"
}

def detect_and_draw(frame):
    results = model(frame)[0]
    detections = []
    for box in results.boxes:
        cls_id = int(box.cls[0])
        conf = float(box.conf[0])
        x1, y1, x2, y2 = map(int, box.xyxy[0])
        label = f"{classes_name.get(cls_id, 'unknown')} {conf:.2f}"
        color = (0, 255, 0) if conf > 0.5 else (0, 0, 255)

        cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
        cv2.putText(frame, label, (x1, y1 - 10),
                    cv2.FONT_HERSHEY_COMPLEX, 0.6, color, 2)

        detections.append({
            "class": classes_name.get(cls_id, "unknown"),
            "confidence": round(conf, 2),
            "bbox": [x1, y1, x2, y2]
        })
    return frame, detections