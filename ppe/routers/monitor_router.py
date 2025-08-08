# routers/monitor_router.py
from fastapi import APIRouter, Request
from fastapi.responses import HTMLResponse
from services.webrtc_service import handle_offer

router = APIRouter(tags=["WebRTC Monitor"])
# 모니터링 서비스 api 개선
# WebRTC 모니터링 페이지와 시그널링 API를 관리
@router.get("/monitor", response_class=HTMLResponse)
async def monitor_page():
    return """
    <html>
    <body>
    <h2>WebRTC Monitor</h2>
    <video id="video" autoplay playsinline controls 
        style="width: 640px; height: 640px; background: black;"></video>
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

@router.post("/offer")
async def offer(request: Request):
    return await handle_offer(request)
