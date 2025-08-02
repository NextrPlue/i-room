# safetyGear/webrtc_server.py
import cv2
import av
import asyncio
from fastapi import FastAPI, Request
from fastapi.responses import HTMLResponse, JSONResponse
from aiortc import RTCPeerConnection, VideoStreamTrack, RTCSessionDescription
from pydantic import BaseModel
from dotenv import load_dotenv
import os

# ===== .env 파일 로드 =====
load_dotenv()

RTSP_URL = os.getenv("RTSP_URL")

app = FastAPI()

# ===== 전역 변수 =====
cap = None
latest_frame = None
capture_task = None
clients_count = 0
lock = asyncio.Lock()

# ===== RTSP 캡처 루프 =====
async def capture_loop():
    global cap, latest_frame, clients_count, last_alert_time
    cap = cv2.VideoCapture(RTSP_URL)
    print("Capture loop started")
    try:
        while True:
            # 클라이언트 없으면 종료
            if clients_count <= 0:
                break

            ret, frame = cap.read()
            if not ret:
                await asyncio.sleep(0.05)
                continue

            latest_frame = frame
            await asyncio.sleep(0.03)
    finally:
        if cap:
            cap.release()
            cap = None
        latest_frame = None
        print("Capture loop stopped")


# ===== WebRTC용 Track =====
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

# ===== HTML 페이지 (간단 테스트용) =====
@app.get("/monitor", response_class=HTMLResponse)
async def monitor_page():
    # JS 부분에 beforeunload 추가됨
    return """
    <html>
    <body>
    <h2>WebRTC Monitor</h2>
    <video id="video" autoplay playsinline controls style="width: 640px; height: 480px; background: black;"></video>
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

# ===== WebRTC 시그널링 엔드포인트 =====
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