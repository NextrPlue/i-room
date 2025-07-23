import cv2
import os
import time
from ultralytics import YOLO

# 모델 로드
model_path = os.path.normpath("{MODEL_PATH}")   # ex) C:/Users/User/i-room-ai/safetyGear/best.pt
model = YOLO(model_path)

# 클래스 정의
class_names = {
    0: "seatbelt_on",
    1: "lanyard_on",
    2: "helmet_on"
}

# WebCam 열기
cap = cv2.VideoCapture(0)
if not cap.isOpened():
    print("웹캠을 열 수 없습니다.")
    exit()

# FPS 측정을 위한 시간 초기화
prev_time = 0

while True:
    ret, frame = cap.read()
    if not ret:
        print("프레임을 읽을 수 없습니다.")
        break

    # YOLO 추론
    results = model(frame)[0]

    # FPS 계산
    curr_time = time.time()
    fps = 1.0 / (curr_time - prev_time)
    prev_time = curr_time

    # 바운딩 박스 직접 그리기
    for box in results.boxes:
        cls_id = int(box.cls[0])
        conf = float(box.conf[0])
        x1, y1, x2, y2 = map(int, box.xyxy[0])

        label = f"{class_names.get(cls_id, 'unknown')} {conf:.2f}"
        color = (0, 255, 0) if conf > 0.5 else (0, 0, 255)

        # 바운딩 박스
        cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)

        # 클래스 라벨
        cv2.putText(frame, label, (x1, y1 - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

    # FPS 출력
    fps_text = f"FPS: {fps:.2f}"
    cv2.putText(frame, fps_text, (10, 30),
                cv2.FONT_HERSHEY_SIMPLEX, 1.0, (255, 255, 0), 2)
    
    # 출력
    cv2.imshow("YOLOv8 Real-Time Detection", frame)

    # 창 닫기 또는 ESC 누르면 종료
    if cv2.getWindowProperty("YOLOv8 Real-Time Detection", cv2.WND_PROP_VISIBLE) < 1:
        break
    if cv2.waitKey(1) & 0xFF == 27:
        break

cap.release()
cv2.destroyAllWindows()