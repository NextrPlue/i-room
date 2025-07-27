# safetyGear/main_video.py
from fastapi import FastAPI
from fastapi.responses import StreamingResponse
import cv2
from safetyGear.utils import detect_and_draw

app = FastAPI()

VIDEO_PATH = "safetyGear/sample.mp4"
cap = cv2.VideoCapture(VIDEO_PATH)

def generate_frames():
    while True:
        ret, frame = cap.read()
        if not ret:
            print("❌ 프레임을 읽을 수 없습니다.")
            cap.set(cv2.CAP_PROP_POS_FRAMES, 0)  # 반복 재생
            continue
        # frame, detections = detect_and_draw(frame)
        _, buffer = cv2.imencode('.jpg', frame)
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')

@app.get("/")
async def root():
    return {"message": "FastAPI YOLO Streaming (Video)"}

@app.get("/stream")
def stream_video():
    return StreamingResponse(generate_frames(), media_type='multipart/x-mixed-replace; boundary=frame')