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
    # YOLO 모델로 프레임(frame) 한 장을 추론
    # model(frame)은 리스트 형태의 결과를 반환하므로 [0]으로 첫 번째 결과만 사용
    results = model(frame)[0]

    detections = []             # 탐지된 객체 정보를 저장할 리스트
    detected_classes = set()    # 탐지된 객체들의 클래스 이름을 저장 (중복 방지됨)

    # 결과 내 각 객체(box)에 대해 반복
    for box in results.boxes:
        # 클래스 ID (정수형)
        cls_id = int(box.cls[0])
        # 탐지 신뢰도(0 ~ 1 사이 값)
        conf = float(box.conf[0])
        # 박스 좌표 (왼쪽 위 x, y, 오른쪽 아래 x, y)
        x1, y1, x2, y2 = map(int, box.xyxy[0])
        
        # 클래스 ID를 이용해 클래스 이름을 가져옴 (classes_name 딕셔너리)
        class_name = classes_name.get(cls_id, 'unknown')

        # 탐지된 클래스 이름을 집합에 추가 (중복 없이)
        detected_classes.add(class_name)

        # 라벨 텍스트 (ex: safety_helmet_on 0.92)
        label = f"{class_name} {conf:.2f}"
        # 신뢰도가 0.5 이상이면 초록색, 아니면 빨간색으로 표시
        color = (0, 255, 0) if conf > 0.5 else (0, 0, 255)

        # 탐지된 객체 영역에 사각형 그리기
        cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
        # 사각형 위쪽에 클래스 이름 + 신뢰도 표시
        cv2.putText(frame, label, (x1, y1 - 10),
                    cv2.FONT_HERSHEY_COMPLEX, 0.6, color, 2)
        
        # detections 리스트에 감지 결과 저장 (추후 서버 전송 등 활용)
        detections.append({
            "class": class_name,            # 클래스 이름
            "confidence": round(conf, 2),   # 소수점 2자리로 반올림된 신뢰도
            "bbox": [x1, y1, x2, y2]        # 바운딩 박스 좌표
        })

    return frame, detections, detected_classes  # 처리된 프레임, 탐지된 모든 객체 리스트, 탐지된 클래스 집합 변환