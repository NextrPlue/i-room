# safetyGear/webrtc_app.py
import cv2
import av
import asyncio
from fastapi import FastAPI, Request
from fastapi.responses import HTMLResponse, JSONResponse
from aiortc import RTCPeerConnection, VideoStreamTrack, RTCSessionDescription
from pydantic import BaseModel
import numpy as np
from safetyGear.utils import detect_and_draw
from deep_sort_realtime.deepsort_tracker import DeepSort
from safetyGear.alert_service import send_alert
import time

app = FastAPI()

# 전역 변수
cap = None
latest_frame = None
capture_task = None
clients_count = 0
lock = asyncio.Lock()
last_alert_time = 0
ALERT_INTERVAL = 5  # 5초 간격
REQUIRED_ITEMS = {"safety_harness_on", "safety_helmet_on"}

# YOLO + DeepSORT 초기화
tracker = DeepSort(max_age=30, n_init=3, max_iou_distance=0.7)

async def capture_loop():
    global cap, latest_frame, clients_count, last_alert_time
    cap = cv2.VideoCapture(0)
    print("Capture loop started")

    frame_count = 0
    fps = 0
    prev_time = time.time()

    try:
        while True:
            # 클라이언트 없으면 종료
            if clients_count <= 0:
                break

            ret, frame = cap.read()
            if not ret:
                await asyncio.sleep(0.05)
                continue

            # YOLO + DeepSORT 처리
            frame, detections, detected_classes = detect_and_draw(frame)
            ds_detections = []
            for det in detections:
                x1, y1, x2, y2 = det['bbox']
                w, h = x2 - x1, y2 - y1
                ds_detections.append(([x1, y1, w, h],
                                      det["confidence"],
                                      det["class"]))
            tracks = tracker.update_tracks(ds_detections, frame=frame)

            for t in tracks:
                if not t.is_confirmed():
                    continue
                x1, y1, x2, y2 = map(int, t.to_ltrb())
                track_id = t.track_id
                cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 255), 2)
                cv2.putText(frame, f"ID:{track_id}", (x1, y1 - 10),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 255), 2)
                
            # FPS 계산
            frame_count += 1
            current_time = time.time()
            if current_time - prev_time >= 1.0:
                fps = frame_count / (current_time - prev_time)
                prev_time = current_time
                frame_count = 0

            # 화면에 FPS 출력
            cv2.putText(frame, f"FPS: {fps:.1f}", (10, 30),
                        cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 0), 2)
                
            # ======== 5초마다 미착용 경고 로직 추가 ========
            missing = REQUIRED_ITEMS - detected_classes
            if missing and (current_time - last_alert_time >= ALERT_INTERVAL):
                # JPEG로 인코딩
                _, img_bytes = cv2.imencode(".jpg", frame)
                send_alert(img_bytes.tobytes(), missing)
                last_alert_time = current_time
            # ============================================

            latest_frame = frame
            await asyncio.sleep(0.03)
    finally:
        if cap:
            cap.release()
            cap = None
        latest_frame = None
        print("Capture loop stopped")


# WebRTC용 Track
class SharedCameraStreamTrack(VideoStreamTrack):
    async def recv(self):
        global latest_frame
        pts, time_base = await self.next_timestamp()
        while latest_frame is None:
            await asyncio.sleep(0.01)
        frame = latest_frame.copy()
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        av_frame = av.VideoFrame.from_ndarray(frame, format="rgb24")
        av_frame.pts = pts
        av_frame.time_base = time_base
        return av_frame

pcs = set()

class Offer(BaseModel):
    sdp: str
    type: str

@app.get("/monitor", response_class=HTMLResponse)
async def monitor_page():
    # JS 부분에 beforeunload 추가됨
    return """
    <html>
    <body>
    <h2>WebRTC Monitor</h2>
    <video id="video" autoplay playsinline controls style="width: 1280px; height: 720px; background: black;"></video>
    <script>
    const pc = new RTCPeerConnection();
    const video = document.getElementById('video');
    pc.ontrack = (event) => { video.srcObject = event.streams[0]; };
    pc.addTransceiver('video', { direction: 'recvonly' });

    window.addEventListener("beforeunload", () => {
        pc.close(); // 브라우저 닫을 때 명시적으로 close
    });

    async function start() {
        const offer = await pc.createOffer();
        await pc.setLocalDescription(offer);
        const resp = await fetch('/offer', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(pc.localDescription)
        });
        const answer = await resp.json();
        await pc.setRemoteDescription(answer);
    }
    start();
    </script>
    </body>
    </html>
    """

@app.post("/offer")
async def offer(request: Request):
    global clients_count, capture_task
    data = await request.json()

    async with lock:
        clients_count += 1
        if capture_task is None or capture_task.done():
            capture_task = asyncio.create_task(capture_loop())

    pc = RTCPeerConnection()
    pcs.add(pc)
    pc.addTrack(SharedCameraStreamTrack())

    @pc.on("connectionstatechange")
    async def on_connectionstatechange():
        global clients_count
        print("Connection state is %s" % pc.connectionState)
        if pc.connectionState in ("failed", "closed", "disconnected"):
            pcs.discard(pc)
            async with lock:
                clients_count -= 1
                # clients_count가 0이면 loop가 자동 종료

    offer_obj = RTCSessionDescription(sdp=data["sdp"], type=data["type"])
    await pc.setRemoteDescription(offer_obj)
    answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)

    return JSONResponse(
        content={"sdp": pc.localDescription.sdp, "type": pc.localDescription.type}
    )

@app.on_event("shutdown")
async def on_shutdown():
    global clients_count
    async with lock:
        clients_count = 0
    coros = [pc.close() for pc in pcs]
    await asyncio.gather(*coros)
    pcs.clear()
