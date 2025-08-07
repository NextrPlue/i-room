import requests
import os
import cv2
from datetime import datetime

SPRING_BOOT_API = "http://localhost:8080/api/alerts"
STATIC_DIR = os.path.join(os.path.dirname(__file__), "static", "alerts")
os.makedirs(STATIC_DIR, exist_ok=True)

def send_alert(frame, helmet_count, seatbelt_count):
    """
    JSON 기반 + 파일 경로 방식 (로컬 개발 환경 전용)
    """
    # 이미지 저장
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"{timestamp}.jpg"
    filepath = os.path.abspath(os.path.join(STATIC_DIR, filename))  # 절대경로 사용
    cv2.imwrite(filepath, frame)

    data = {
        "helmet_count": helmet_count,
        "seatbelt_count": seatbelt_count,
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "image_path": filepath   # URL 대신 파일 경로 전달
    }

    try:
        r = requests.post(SPRING_BOOT_API, json=data, timeout=5)
        print(f"[INFO] Alert sent: {r.status_code}, Path: {filepath}")
    except Exception as e:
        print(f"[ERROR] Failed to send alert: {e}")
