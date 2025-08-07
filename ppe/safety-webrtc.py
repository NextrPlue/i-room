import cv2
import av
import asyncio
import torch
import time
import psutil
import logging
from fastapi import FastAPI, Request
from fastapi.responses import HTMLResponse, JSONResponse
from aiortc import RTCPeerConnection, VideoStreamTrack, RTCSessionDescription
from pydantic import BaseModel
from alert_service import send_alert
from ultralytics import YOLO
import os
from dotenv import load_dotenv
from datetime import datetime

## 헬멧 수와 안전벨트 수가 동일하지 않다면 알림 전송

# 환경설정
# ===== .env 로드 =====
load_dotenv() # .env 파일에서 환경변수 불러오기
RTSP_URL = os.getenv("RTSP_URL") # cctv 카메라 주소
MODEL_PATH = os.getenv("MODEL_PATH", "model/best_8m_v4.pt") # YOLO 모델 가중치 파일 경로

# RTSP_URL이 없으면 test2.mp4로 대체
if not RTSP_URL or (not RTSP_URL.lower().startswith("rtsp") and not os.path.exists(RTSP_URL)):
    print("[WARN] RTSP_URL이 설정되지 않았거나 잘못됨 → test2.mp4 사용")
    BASE_DIR = os.path.dirname(os.path.abspath(__file__))
    RTSP_URL = r"C:\Users\kalin\i-room-ai\safetyGear\test2.mp4"

app = FastAPI()

# ===== 전역 변수 =====
cap = None
latest_frame = None # 최근 프레임 저장 (브라우저 전송용)
capture_task = None # 영상 캡처 작업(task) 관리
clients_count = 0   # 현재 접속 중인 클라이언트 수
lock = asyncio.Lock()
last_alert_time = 0
ALERT_INTERVAL = 5   # 초 단위 알람 최소 간격
frame_index = 0
mismatch_count = 0  # 불일치 지속 프레임 수
THRESHOLD_FRAMES = 150  # 보호구 불일치 지속 프레임 수 (예: 60프레임 ≈2초 @30fps)

# 클래스 이름 매핑
CLASS_NAMES = {
    0: "seatbelt_on",
    1: "helmet_on",
}

# 장치 설정, GPU 설정
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Using device: {device}")
if device == "cuda":
    model = YOLO(MODEL_PATH).to(device).half()
    print("FP16(Half precision) 모드 활성화")
else:
    model = YOLO(MODEL_PATH).to(device)
    print("CPU 모드: FP16 미지원, FP32로 실행")

logging.basicConfig(filename="botsort_inference_log.txt", level=logging.INFO)
process = psutil.Process()


# ===== RTSP 캡처 루프 =====
async def capture_loop():
    global cap, latest_frame, clients_count, last_alert_time
    global frame_index, mismatch_count

    cap = cv2.VideoCapture(RTSP_URL, cv2.CAP_FFMPEG)
    print(f"Capture loop started → Source: {RTSP_URL}")

    prev_time = time.time()
    frame_count = 0
    fps = 0

    # RTSP 캡처 및 YOLO 추적
    try:
        results = model.track(
            source=RTSP_URL,
            tracker="my_botsort.yaml",
            stream=True,
            device=device,
            persist=True,
            half=True,
            conf=0.2,
        )

        for r in results:
            if clients_count <= 0:
                break

            frame = r.orig_img.copy()
            frame_index += 1

            orig_h, orig_w = frame.shape[:2]
            target_w, target_h = 640, 640
            frame = cv2.resize(frame, (target_w, target_h))

            # FPS 계산
            frame_count += 1
            elapsed = time.time() - prev_time
            if elapsed >= 1.0:
                fps = frame_count / elapsed
                frame_count = 0
                prev_time = time.time()

            cv2.putText(frame, f"FPS: {fps:.2f}", (20, 40),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 255), 2)

            # -------------------- 객체 카운트 --------------------
            # 객체 감지 및 불일치 체크
            helmet_count = 0
            seatbelt_count = 0

            for box in r.boxes:
                x1, y1, x2, y2 = map(float, box.xyxy[0])
                scale_x = target_w / orig_w
                scale_y = target_h / orig_h
                x1, y1, x2, y2 = int(x1 * scale_x), int(y1 * scale_y), int(x2 * scale_x), int(y2 * scale_y)

                cls_id = int(box.cls[0])
                conf = float(box.conf[0])
                track_id = int(box.id[0]) if box.id is not None else -1

                label = f"ID {track_id} | {CLASS_NAMES.get(cls_id, str(cls_id))} {conf:.2f}"
                color = (0, 255, 0)

                cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
                cv2.putText(frame, label, (x1, y1 - 10),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

                if cls_id == 0:
                    seatbelt_count += 1
                elif cls_id == 1:
                    helmet_count += 1

            # -------------------- 불일치 감지 --------------------
            if helmet_count != seatbelt_count:
                mismatch_count += 1
            else:
                mismatch_count = 0

            # -------------------- 알람 발생 --------------------
            current_time = time.time()
            if mismatch_count >= THRESHOLD_FRAMES and (current_time - last_alert_time >= ALERT_INTERVAL):
                _, img_bytes = cv2.imencode(".jpg", frame)
                missing_items = {"helmet_or_seatbelt"}
                send_alert(img_bytes.tobytes(), missing_items)
                print(f"[ALERT] 보호구 불일치 감지! helmet={helmet_count}, seatbelt={seatbelt_count}")
                last_alert_time = current_time
                mismatch_count = 0

            latest_frame = frame
            await asyncio.sleep(0)

    finally:
        if cap:
            cap.release()
            cap = None
        latest_frame = None
        print("Capture loop stopped")


# ===== WebRTC용 Track, 영상 스트리밍 =====
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
    return """
    <html>
    <body>
    <h2>WebRTC Monitor</h2>
    <video id="video" autoplay playsinline controls style="width: 640px; height: 640px; background: black;"></video>
    <script>
    const pc = new RTCPeerConnection();
    const video = document.getElementById('video');
    pc.ontrack = (event) => { video.srcObject = event.streams[0]; };
    pc.addTransceiver('video', { direction: 'recvonly' });

    window.addEventListener("beforeunload", () => { pc.close(); });

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

# ===== FastAPI 실행부 =====
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)
