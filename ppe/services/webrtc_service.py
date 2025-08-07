import cv2
import av
import asyncio
from aiortc import RTCPeerConnection, VideoStreamTrack
from fastapi import Request
from fastapi.responses import JSONResponse

frame_queue = asyncio.Queue(maxsize=1)
pcs = set()
lock = asyncio.Lock()
clients_count = 0

# WebRTC 연결 및 영상 스트리밍을 담당

class SharedCameraStreamTrack(VideoStreamTrack):
    async def recv(self):
        pts, time_base = await self.next_timestamp()
        frame = await frame_queue.get()
        frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        av_frame = av.VideoFrame.from_ndarray(frame, format="rgb24")
        av_frame.pts = pts
        av_frame.time_base = time_base
        return av_frame

async def handle_offer(request: Request):
    global clients_count
    data = await request.json()
    async with lock:
        clients_count += 1

    pc = RTCPeerConnection()
    pcs.add(pc)
    pc.addTrack(SharedCameraStreamTrack())

    @pc.on("connectionstatechange")
    async def on_connectionstatechange():
        global clients_count
        if pc.connectionState in ("failed", "closed", "disconnected"):
            pcs.discard(pc)
            async with lock:
                clients_count -= 1

    from aiortc import RTCSessionDescription
    offer_obj = RTCSessionDescription(sdp=data["sdp"], type=data["type"])
    await pc.setRemoteDescription(offer_obj)
    answer = await pc.createAnswer()
    await pc.setLocalDescription(answer)

    return JSONResponse(
        content={"sdp": pc.localDescription.sdp, "type": pc.localDescription.type}
    )
