# safetyGear/alert_service.py
import requests
from datetime import datetime

# Spring Boot 서버 API 주소 (REST API)
# 미착용 감지 시 캡처된 이미지와 정보를 보낼 엔드포인트
SPRING_BOOT_API = "http://localhost:8080/api/alerts"

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
        "missing_items": ", ".join(missing_items),
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    }

    try:
        # POST 요청으로 Spring Boot API에 전송
        r = requests.post(SPRING_BOOT_API, data=data, files=files, timeout=3)
        print(f"[INFO] Alert sent: {r.status_code}")
    except Exception as e:
        print(f"[ERROR] Failed to send alert: {e}")