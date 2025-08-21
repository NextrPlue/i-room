# services/webrtc_service.py
import asyncio
import time
from typing import List

import av
import cv2
from fastapi import Request
from fastapi.responses import JSONResponse

from aiortc import (
    RTCPeerConnection,
    RTCSessionDescription,
    VideoStreamTrack,
    RTCConfiguration,
    RTCIceServer,
)

from ppe.utils.turn_auth import make_turn_credential
from ppe.config import settings

# 송출 품질(부하) 파라미터
TARGET_WIDTH = 640      # 스트림 가로 해상도 상한(원본이 더 크면 다운스케일)
TARGET_FPS   = 15       # 송출 FPS 상한

# 최신 프레임만 유지
frame_queue: asyncio.Queue = asyncio.Queue(maxsize=1)

# 접속 관리
pcs: set[RTCPeerConnection] = set()
lock = asyncio.Lock()
clients_count = 0


class SharedCameraStreamTrack(VideoStreamTrack):
    """frame_queue에 들어오는 최신 프레임을 브라우저로 전송"""
    def __init__(self) -> None:
        super().__init__()
        self._last_ts = 0.0

    async def recv(self):
        pts, time_base = await self.next_timestamp()
        frame = await frame_queue.get()  # BGR(OpenCV)

        # 다운스케일(가로 기준)
        h, w = frame.shape[:2]
        if w > TARGET_WIDTH:
            nh = int(h * TARGET_WIDTH / w)
            frame = cv2.resize(frame, (TARGET_WIDTH, nh), interpolation=cv2.INTER_AREA)

        # FPS 제한
        wait = (1.0 / TARGET_FPS) - (time.time() - self._last_ts)
        if wait > 0:
            await asyncio.sleep(wait)
        self._last_ts = time.time()

        # RGB 변환 없이 바로 bgr24로 보내면 CPU 부담이 확 줄어듦
        av_frame = av.VideoFrame.from_ndarray(frame, format="bgr24")
        av_frame.pts = pts
        av_frame.time_base = time_base
        return av_frame


def _build_ice_servers() -> List[RTCIceServer]:
    """
    .env 설정을 바탕으로 ICE 서버 목록 구성.
    - ICE_FORCE_RELAY=true 이면 STUN 제외 → 사실상 항상 TURN 사용
    - TURN(HMAC 일회성) 자격증명 생성
    """
    servers: List[RTCIceServer] = []

    if not settings.ICE_FORCE_RELAY:
        if getattr(settings, "STUN_URLS", None):
            stun_urls = [u.strip() for u in settings.STUN_URLS.split(",") if u.strip()]
            if stun_urls:
                servers.append(RTCIceServer(urls=stun_urls))

    cred = make_turn_credential("srv")
    servers.append(
        RTCIceServer(
            urls=cred["urls"],            # ["turn:IP:3478?transport=udp", "turn:IP:3478?transport=tcp"]
            username=cred["username"],
            credential=cred["credential"],
        )
    )
    return servers


def _prefer_h264(pc: RTCPeerConnection) -> None:
    """가능하면 H.264 코덱을 우선 사용(없으면 조용히 스킵)."""
    try:
        from aiortc.rtcrtpsender import RTCRtpSender
        h264 = [c for c in RTCRtpSender.getCapabilities("video").codecs if c.mimeType == "video/H264"]
        if not h264:
            return
        for tr in pc.getTransceivers():
            if tr.kind == "video":
                tr.setCodecPreferences(h264)
    except Exception:
        # ffmpeg/libx264 없는 환경이면 그냥 넘어감
        pass


def _make_pc() -> RTCPeerConnection:
    cfg = RTCConfiguration(iceServers=_build_ice_servers())
    pc = RTCPeerConnection(configuration=cfg)
    return pc


async def _wait_ice_gathering_complete(pc: RTCPeerConnection, timeout: float = 5.0):
    """트리클 ICE를 쓰지 않으니, 후보가 SDP에 포함되도록 잠시 대기."""
    if pc.iceGatheringState == "complete":
        return

    done = asyncio.Event()

    @pc.on("icegatheringstatechange")
    async def _on_state_change():
        if pc.iceGatheringState == "complete":
            done.set()

    try:
        await asyncio.wait_for(done.wait(), timeout=timeout)
    except asyncio.TimeoutError:
        # 오래 걸리면 그냥 진행
        pass


async def handle_offer(request: Request):
    """
    브라우저 Offer 수신 → 서버 PC 생성 → 비디오 트랙 추가 → Answer 반환.
    """
    global clients_count
    data = await request.json()

    pc = _make_pc()
    pcs.add(pc)

    # 서버 → 브라우저 단방향 송출(sendonly)
    sender = pc.addTrack(SharedCameraStreamTrack())
    _prefer_h264(pc)  # 가능하면 H.264 우선

    @pc.on("connectionstatechange")
    async def on_connectionstatechange():
        global clients_count
        state = pc.connectionState
        if state == "connected":
            async with lock:
                clients_count += 1
        if state in ("failed", "closed", "disconnected"):
            if pc in pcs:
                pcs.discard(pc)
            try:
                await pc.close()
            finally:
                async with lock:
                    clients_count = max(0, clients_count - 1)

    # SDP 교환
    offer_obj = RTCSessionDescription(sdp=data["sdp"], type=data["type"])
    await pc.setRemoteDescription(offer_obj)

    answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)

    await _wait_ice_gathering_complete(pc)

    return JSONResponse(
        content={"sdp": pc.localDescription.sdp, "type": pc.localDescription.type}
    )
