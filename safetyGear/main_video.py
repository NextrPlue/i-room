# safetyGear/main_video.py
from fastapi import FastAPI
from fastapi.responses import StreamingResponse
import cv2
from deep_sort_realtime.deepsort_tracker import DeepSort
from safetyGear.utils import detect_and_draw

app = FastAPI()

VIDEO_PATH = "safetyGear/sample.mp4"
cap = cv2.VideoCapture(VIDEO_PATH)

# DeepSORT 초기화
tracker = DeepSort(
    max_age=30,
    n_init=3,
    max_iou_distance=0.7
)

def generate_frames():
    while True:
        ret, frame = cap.read()
        if not ret:
            print("❌ 프레임을 읽을 수 없습니다.")
            cap.set(cv2.CAP_PROP_POS_FRAMES, 0)  # 반복 재생
            continue

        # 1. YOLO detection
        frame, detections, detected_classes = detect_and_draw(frame)

        # 2. DeepSORT tracking
        ds_detections = []
        for det in detections:
            x1, y1, x2, y2 = det["bbox"]
            w, h = x2 - x1, y2 - y1
            ds_detections.append(([x1, y1, w, h],
                                   det["confidence"],
                                   det["class"]))

        tracks = tracker.update_tracks(ds_detections, frame=frame)

        # 3. tracking ID 그리기
        for t in tracks:
            if not t.is_confirmed():
                continue
            x1, y1, x2, y2 = map(int, t.to_ltrb())
            track_id = t.track_id
            cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 255), 2)
            cv2.putText(frame, f"ID:{track_id}", (x1, y1 - 10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 255), 2)

        # 4. 스트리밍
        _, buffer = cv2.imencode('.jpg', frame)
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')

@app.get("/")
async def root():
    return {"message": "FastAPI YOLO Streaming (Video)"}

@app.get("/stream")
def stream_video():
    return StreamingResponse(generate_frames(),
                             media_type='multipart/x-mixed-replace; boundary=frame')
