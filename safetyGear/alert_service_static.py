import requests
import os
from datetime import datetime
import cv2

# Spring Boot 서버 API 주소
SPRING_BOOT_API = "http://localhost:8080/api/alerts"

# 정적 파일 저장 경로
STATIC_DIR = os.path.join(os.path.dirname(__file__), "static", "alerts")
os.makedirs(STATIC_DIR, exist_ok=True)

def send_alert(frame, helmet_count, seatbelt_count):
    """
    Spring Boot 서버로 이미지 + 탐지 정보 전송
    frame: numpy.ndarray (OpenCV 프레임)
    helmet_count: 탐지된 헬멧 수
    seatbelt_count: 탐지된 안전벨트 수
    """

    # 타임스탬프 기반 파일명 생성
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"{timestamp}.jpg"
    filepath = os.path.join(STATIC_DIR, filename)

    # 이미지 저장
    cv2.imwrite(filepath, frame)

    # URL 생성 (FastAPI에서 /static으로 접근 가능)
    image_url = f"http://127.0.0.1:8000/static/alerts/{filename}"

    # 전송할 데이터
    data = {
        "helmet_count": helmet_count,
        "seatbelt_count": seatbelt_count,
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "image_url": image_url
    }

    try:
        r = requests.post(SPRING_BOOT_API, json=data, timeout=5)
        print(f"[INFO] Alert sent: {r.status_code}, URL: {image_url}")
    except Exception as e:
        print(f"[ERROR] Failed to send alert: {e}")
