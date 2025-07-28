# safetyGear/main_webcam.py
from fastapi import FastAPI
from fastapi.responses import StreamingResponse
import cv2
from safetyGear.utils import detect_and_draw
import time
import requests

# Spring Boot 서버 API 주소 (REST API)
# 미착용 감지 시 캡처된 이미지와 정보를 보낼 엔드포인트
SPRING_BOOT_API = ""

app = FastAPI()             # FastAPI 인스턴스 생성
cap = cv2.VideoCapture(0)   # 웹캠

# 안전 보호구 종류
required_items = {"safety_harness_on", "safety_lanyard_on", "safety_helmet_on"}

# 알림(미착용 이벤트) 전송 함수
def send_alert(image_bytes, missing_items):
    """
    Spring Boot 서버로 이미지 + 부가 정보를 전송
    image_bytes: 캡처된 프레임 이미지 (바이트)
    missing_items: 누락된 보호구 클랫 이름 집합
    """

    # HTTP multipart/form-data 로 전송할 파일(이미지)
    files = {
        "image": ("frame.jpg", image_bytes, "image/jpeg")
    }

    # 추가 데이터: 미착용 장비 목록, 현재 시간
    data = {
        "missing_items": ",".join(missing_items), 
        "timestamp": str(time.time())
    }

    try:
        # POST 요청으로 Spring Boot API에 전송
        r = requests.post(SPRING_BOOT_API, data=data, files=files, timeout=3)
        print(f"[INFO] Alert sent: {r.status_code}")
    except Exception as e:
        print(f"[ERROR] Failed to send alert: {e}")

# 웹캠 프레임을 계속 읽어 스트리밍 + 알림 처리하는 제너레이터
def generate_frames():
    while True:
        # 웹캠에서 프레임 한 장 읽기
        ret, frame = cap.read()
        if not ret:
            print("[ERROR] 프레임을 읽을 수 없습니다.")
            break   # 카메라가 꺼지거나 오류 발생 시 종료

        # YOLO 탐지 실행 -> 객체 감지 후 Bounding Box를 그린 프레임과 감지 정보 반환
        frame, detections, detected_classes = detect_and_draw(frame)

        # 미착용 여부 판단
        # 필수 보호구(required_items) - 실제 감지된 보호구(detected_classes)
        # 차집합을 구해 누락된 보호구가 있으면 missing에 들어감
        missing = required_items - detected_classes

        if missing:
            # 누락된 보호구가 있을 경우, 프레임 이미지를 JPEG 바이트로 변환
            _, img_bytes = cv2.imencode(".jpg", frame)

            # 서버로 알림 전송 (이미지 + 누락 정보)
            send_alert(img_bytes.tobytes(), missing)

        # 스트리밍할 프레임 인코딩
        # 프레임을 JPEG 포맷으로 변환 (HTTP로 전송하기 위함)
        _, buffer = cv2.imencode('.jpg', frame)

        # HTTP 멀티파트 응답 포맷으로 프레임 전송
        yield (b'--frame\r\n'
               b'Content-Type: image/jpeg\r\n\r\n' + buffer.tobytes() + b'\r\n')

# API 엔드포인트 1: 기본 확인용
@app.get("/")
async def root():
    # 서버 정상 동작 여부를 확인하기 위한 메시지
    return {"message": "FastAPI YOLO Streaming (Webcam)"}

# API 엔드포인트 2: 실시간 스트리밍
@app.get("/stream")
def stream_video():
    # 스트리밍 응답 (브라우저에서 /stream URL 접속하면 실시간 영상 확인 가능)
    return StreamingResponse(generate_frames(), media_type='multipart/x-mixed-replace; boundary=frame')