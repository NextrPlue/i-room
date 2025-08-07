from fastapi import FastAPI
from ultralytics import YOLO
import cv2
import requests
from fastapi.responses import StreamingResponse
import os

app = FastAPI() # FastAPI 인스턴스 생성

# 현재 파일 기준 경로로 best.pt 찾기
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "model", "best.pt")

# 모델 로드
model = YOLO(MODEL_PATH)

# 탐지해야 하는 객체들의 클래스 이름
classes_name = {
    0: "safety_harness_on",
    1: "safety_lanyard_on",
    2: "safety_helmet_on"
}

# Spring Boot 서버 주소
# SPRING_API_URL = ""

# 웹캠 열기
cap = cv2.VideoCapture(0)

def generate_frames():
    while True:
        ret, frame = cap.read()
        if not ret:
            break

        # YOLO 추론
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

        # Spring Boot로 감지 결과 전송
        # try:
        #     requests.post(SPRING_API_URL, json={"results": detections})
        # except Exception as e:
        #     print("Spring Boot로 전송 실패:", e)

        # 프레임을 JPEG로 인코딩
        _, buffer = cv2.imencode('.jpg', frame)
        frame_bytes = buffer.tobytes()

        # MJPEG 스트리밍 응답
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + frame_bytes + b'\r\n')

@app.get("/")
async def root():
    return {"message": "FastAPI YOLO Streaming"}

# 실시간 웹캠 영상
@app.get("/stream")
def stream_video():
    return StreamingResponse(generate_frames(), media_type='multipart/x-mixed-replace; boundary=frame')