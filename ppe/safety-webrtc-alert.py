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
from alert_service_static import send_alert
from ultralytics import YOLO
import os
from dotenv import load_dotenv
from datetime import datetime

# === 환경설정 ===============================================================
load_dotenv()   # 환경변수 로딩 (.env 파일의 환경변수를 로드)

# RTSP 카메라 주소
RTSP_URL = os.getenv("RTSP_URL")

# 경로 설정
BASE_DIR = os.path.dirname(os.path.abspath(__file__))           # 현재 파일(safety-webrtc-alert.py) 기준 디렉토리 경로(safetyGear)
MODEL_PATH = os.path.join(BASE_DIR, "model", "best_8m_v4.pt")   # safetyGear/model/best_8m_v4.py

# FPS 표시 여부 설정
SHOW_FPS = os.getenv("SHOW_FPS", "false").lower() == "true"

# RTSP_URL이 비어있거나, 잘못된 경우 -> sample 동영상으로 대체
if not RTSP_URL or (not RTSP_URL.lower().startswith("rtsp") and not os.path.exists(RTSP_URL)):
    print("[WARN] RTSP_URL이 설정되지 않았거나 잘못됨 → test2.mp4 사용")
    RTSP_URL = os.path.join(BASE_DIR, "test2.mp4")  # safetyGear/test2.mp4
# ===========================================================================

# FastAPI 실행
app = FastAPI()

# === 전역 변수 ==============================================================
cap = None              # OpenCV VideoCapture 객체 (실제 영상 입력 장치)
capture_task = None     # asyncio에서 실행되는 비동기 캡처 루프
clients_count = 0       # 연결된 WebRTC 클라이언트 수
lock = asyncio.Lock()   # 클라이언트 수 변경 시 동시 접근 방지
frame_index = 0
frame_queue = asyncio.Queue(maxsize=1)  # 항상 최신 프레임만 보관 (WebRTC 송출 시 사용)
# ===========================================================================

# YOLO 모델의 클래스 ID와 실제 라벨 이름 매핑
CLASS_NAMES = {
    0: "safety_belt_on",    # 안전벨트
    1: "helmet_on",         # 안전모
}

# GPU 사용 가능 여부 확인 =====================================================
device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"Using device: {device}")

if device == "cuda":
    model = YOLO(MODEL_PATH).to(device).half()  # 장치가 GPU면 FP16(half precision)으로 모델 로딩 -> 속도 향상
    print("FP16(Half precision) 모드 활성화")
else:
    model = YOLO(MODEL_PATH).to(device)         # 장치가 CPU면 FP32(기본)로 로딩
    print("CPU 모드: FP16 미지원, FP32로 실행")
# ===========================================================================

# 모델 추론 결과에 대한 로그 저장
logging.basicConfig(filename="botsort_inference_log.txt", level=logging.INFO)
process = psutil.Process()

# === 영상 캡처 + YOLO (객체 탐지) + BoT-SORT (객체 추적) + 알람 전송 ============
async def capture_loop():
    global cap, clients_count, frame_index

    # FPS 계산용
    prev_time = time.time()
    frame_count = 0
    fps = 0

    # 10초 단위로 헬멧, 안전벨트 착용 여부를 집계
    interval_start = time.time()
    INTERVAL_SEC = 10
    helmet_ids = set()
    seatbelt_ids = set()
    total_frames = 0

    while clients_count > 0:   # 클라이언트 있을 때만 계속
        try:
            cap = cv2.VideoCapture(RTSP_URL, cv2.CAP_FFMPEG)    # RTSP/IP 카메라 재연결 가능성을 고려해서 while 안에서 VideoCapture 생성
            if not cap.isOpened():
                print("[ERROR] 영상 소스를 열수 없음. 2초 후 재시도.")
                await asyncio.sleep(2)
                continue

            print(f"[INFO] 캡처 시작 → Source: {RTSP_URL}")

            while clients_count > 0 and cap.isOpened():
                ret, frame = cap.read()
                if not ret:
                    print("[WARN] 프레임을 읽을 수 없음. 캡처 종료 후 재시도 예정")
                    break

            # FPS 계산
            frame_index += 1

            # Resize
            orig_h, orig_w = frame.shape[:2]
            target_w, target_h = 640, 640
            frame = cv2.resize(frame, (target_w, target_h))

            # YOLO 결과
            results = model.predict(frame, conf=0.2, verbose=False)[0]

            # YOLO 결과 영상에 반영
            for box in results.boxes:
                x1, y1, x2, y2 = map(float, box.xyxy[0])
                scale_x = target_w / orig_w
                scale_y = target_h / orig_h
                x1, y1, x2, y2 = int(x1 * scale_x), int(y1 * scale_y), int(x2 * scale_x), int(y2 * scale_y)
                cls_id = int(box.cls[0])
                conf = float(box.conf[0])
                track_id = -1

                label = f"ID {track_id} | {CLASS_NAMES.get(cls_id, str(cls_id))} {conf:.2f}"
                color = (0, 255, 0)

                cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
                cv2.putText(frame, label, (x1, y1 - 10),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

                if cls_id == 0:
                    seatbelt_ids.add(track_id)
                elif cls_id == 1:
                    helmet_ids.add(track_id)

            for r in results:
                if clients_count <= 0:
                    await asyncio.sleep(0.5)
                    if clients_count <= 0:
                        print("[DEBUG] No clients after wait → stopping capture loop")
                        break

                if r is None or r.orig_img is None:
                    print(f"[ERROR] Received empty frame at index={frame_index}")
                    break

                # FPS 계산 (옵션)
                frame_count += 1
                elapsed = time.time() - prev_time
                if elapsed >= 1.0:
                    fps = frame_count / elapsed
                    frame_count = 0
                    prev_time = time.time()

                if SHOW_FPS:
                    cv2.putText(frame, f"FPS: {fps:.2f}", (20, 40),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 255), 2)

                total_frames += 1

                current_time = time.time()
                if current_time - interval_start >= INTERVAL_SEC:   # 매 10초마다 send_alert() 호출 -> 서버/대시보드로 전송
                    helmet_count = len(helmet_ids)
                    seatbelt_count = len(seatbelt_ids)

                    helmet_ratio = helmet_count / total_frames if total_frames > 0 else 0
                    seatbelt_ratio = seatbelt_count / total_frames if total_frames > 0 else 0

                    send_alert(frame, helmet_count, seatbelt_count)

                    print(f"[REPORT] 10초 요약: helmet_count={helmet_count}, seatbelt_count={seatbelt_count}, "
                          f"helmet_ratio={helmet_ratio:.2f}, seatbelt_ratio={seatbelt_ratio:.2f}")

                    # 집계 초기화 후, 다음 인터벌 시작
                    interval_start = current_time
                    helmet_ids.clear()
                    seatbelt_ids.clear()
                    total_frames = 0

                # 최신 프레임만 queue에 유지 (WebRTC 전송 시 지연 방지)
                if not frame_queue.empty():
                    try:
                        frame_queue.get_nowait()  # 이전 프레임 제거
                    except asyncio.QueueEmpty:
                        pass
                await frame_queue.put(frame)

        except Exception as e:
            print(f"[ERROR] Exception in capture loop: {e}")

        finally:
            if cap:
                cap.release()
                cap = None
            print(f"Capture loop stopped at frame {frame_index}")

        if RTSP_URL.endswith(".mp4"):
            break

# ===== WebRTC용 Track =====
class SharedCameraStreamTrack(VideoStreamTrack):
    async def recv(self):
        pts, time_base = await self.next_timestamp()
        # 최신 프레임 대기
        frame = await frame_queue.get()
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        av_frame = av.VideoFrame.from_ndarray(frame, format="rgb24")
        av_frame.pts = pts
        av_frame.time_base = time_base
        return av_frame

pcs = set()

class Offer(BaseModel):
    sdp: str
    type: str

# ===== HTML 페이지 =====
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

# ===== WebRTC 시그널링 =====
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

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)
