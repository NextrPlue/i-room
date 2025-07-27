# safetyGear/main_webcam.py
from fastapi import FastAPI
from fastapi.responses import StreamingResponse
import cv2
from safetyGear.utils import detect_and_draw

# FastAPI 인스턴스 생성
app = FastAPI()

cap = cv2.VideoCapture(0)  # 웹캠

def generate_frames():
    while True:
        ret, frame = cap.read()
        if not ret:
            break
        frame, detections = detect_and_draw(frame)
        _, buffer = cv2.imencode('.jpg', frame)
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')

@app.get("/")
async def root():
    return {"message": "FastAPI YOLO Streaming (Webcam)"}

@app.get("/stream")
def stream_video():
    return StreamingResponse(generate_frames(), media_type='multipart/x-mixed-replace; boundary=frame')