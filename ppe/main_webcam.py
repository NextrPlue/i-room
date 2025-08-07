# safetyGear/main_webcam.py
from fastapi import FastAPI
from fastapi.responses import StreamingResponse
from safetyGear.camera_stream import generate_frames

app = FastAPI() # FastAPI 인스턴스 생성

# API 엔드포인트 1: 기본 확인용
@app.get("/")
async def root():
    # 서버 정상 동작 여부를 확인하기 위한 메시지
    return {"message": "FastAPI YOLO Streaming (Webcam)"}

# API 엔드포인트 2: 실시간 스트리밍
@app.get("/stream")
def stream_video():
    # 스트리밍 응답 (브라우저에서 /stream URL 접속하면 실시간 영상 확인 가능)
    return StreamingResponse(
        generate_frames(), 
        media_type='multipart/x-mixed-replace; boundary=frame'
    )