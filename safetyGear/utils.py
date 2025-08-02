# safetyGear/utils.py
from ultralytics import YOLO
import cv2
import os
import torch

# ===== YOLO 모델 + BoT-SORT 추적기 설정 =====
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "model", "best_8m_v4.pt")
TRACKER_CONFIG = os.path.join(BASE_DIR, "my_botsort.yaml")

# ===== GPU 설정 =====
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Using device: {device}")

# ===== FP16 모드 적용 (CUDA 전용) =====
if device == "cuda":
    model = YOLO(MODEL_PATH).to(device).half()
    print("FP16(Half precision) 모드 활성화")
else:
    model = YOLO(MODEL_PATH).to(device)
    print("CPU 모드: FP16 미지원, FP32로 실행")

# ===== 탐지해야 하는 객체들의 클래스 이름 =====
classes_name = {
    0: "safety_belt",
    1: "safety_helmet"
}

def detect_and_draw(frame):
    # BoT-SORT 기반 추적
    results = model.track(
        source=frame,
        persist=True,
        stream=False,
        tracker=TRACKER_CONFIG,
        verbose=False
    )[0]

    detections = []             # 탐지된 객체 정보를 저장할 리스트
    detected_classes = set()    # 탐지된 객체들의 클래스 이름을 저장 (중복 방지됨)

    # 결과 내 각 객체(box)에 대해 반복
    for box in results.boxes:
        cls_id = int(box.cls[0])    # 클래스 ID (정수형)
        conf = float(box.conf[0])   # 탐지 신뢰도(0 ~ 1 사이 값)
        x1, y1, x2, y2 = map(int, box.xyxy[0])  # 박스 좌표 (왼쪽 위 x, y, 오른쪽 아래 x, y)
        class_name = classes_name.get(cls_id, 'unknown')    # 클래스 ID를 이용해 클래스 이름을 가져옴 (classes_name 딕셔너리)
        detected_classes.add(class_name)    # 탐지된 클래스 이름을 집합에 추가 (중복 없이)
        
        # track ID (BoT-SORT가 제공)
        track_id = None
        if hasattr(box, 'id') and box.id is not None:
            track_id = int(box.id[0])

        # 화면에 바로 그리기
        color = (0, 255, 0) if conf > 0.5 else (0, 0, 255)  # 신뢰도가 0.5 이상이면 초록색, 아니면 빨간색으로 표시
        label = f"{class_name} {conf:.2f}"  # 라벨 텍스트 (ex: safety_helmet 0.92)
        cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)  # 탐지된 객체 영역에 사각형 그리기
        cv2.putText(frame, label, (x1, y1 - 10),            # 사각형 위쪽에 클래스 이름 + 신뢰도 표시
                    cv2.FONT_HERSHEY_COMPLEX, 0.6, color, 2)
        
        if track_id is not None:
            cv2.putText(frame, f"ID:{track_id}", (x1, y1 - 25),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 255), 2)

        # detections 리스트에 감지 결과 저장 (추후 서버 전송 등 활용)
        detections.append({
            "class": class_name,            # 클래스 이름
            "confidence": round(conf, 2),   # 소수점 2자리로 반올림된 신뢰도
            "bbox": [x1, y1, x2, y2],       # 바운딩 박스 좌표
            "track_id": track_id
        })

    return frame, detections, detected_classes  # 처리된 프레임, 탐지된 모든 객체 리스트, 탐지된 클래스 집합 변환