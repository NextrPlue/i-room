import requests
import os
from datetime import datetime
import cv2
from sqlalchemy.orm import Session
from database import SessionLocal
from orm.violation import Violation

# Spring Boot API endpoint
SPRING_BOOT_API = "http://localhost:8080/api/alerts"

# 이미지 저장 경로
STATIC_DIR = os.path.join(os.path.dirname(__file__), "static", "alerts")
os.makedirs(STATIC_DIR, exist_ok=True)


def send_alert_if_violation(violation_id: int, frame):
    """
    violation 테이블에 저장된 내용을 기반으로 Spring Boot 서버로 알림 전송
    """
    db: Session = SessionLocal()
    try:
        # 1. violation 레코드 조회
        violation = db.query(Violation).filter(Violation.id == violation_id).first()
        if not violation:
            print(f"[WARN] Violation record not found (id={violation_id})")
            return

        # 2. 이미지 저장
        timestamp_str = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"{timestamp_str}.jpg"
        filepath = os.path.join(STATIC_DIR, filename)
        cv2.imwrite(filepath, frame)

        # 3. 이미지 URL 생성 (FastAPI static 사용 중 가정)
        image_url = f"http://127.0.0.1:8000/static/alerts/{filename}"

        # 4. image_url 갱신 (DB에도 저장된 값 덮어쓰기)
        violation.image_url = image_url
        db.commit()
        db.refresh(violation)

        # 5. 전송 데이터 구성 (테이블 전체 필드 그대로)
        data = {
            "id": violation.id,
            "worker_id": violation.worker_id,
            "ppe_id": violation.ppe_id,
            "image_url": violation.image_url,
            "latitude": violation.latitude,
            "longitude": violation.longitude,
            "timestamp": violation.timestamp.strftime("%Y-%m-%d %H:%M:%S"),
            "helmet_on_count": violation.helmet_on_count,
            "seatbelt_on_count": violation.seatbelt_on_count
        }

        # 6. POST 요청 전송
        response = requests.post(SPRING_BOOT_API, json=data, timeout=5)
        print(f"[INFO] Alert sent to Spring Boot: {response.status_code} ({response.reason})")

    except Exception as e:
        print(f"[ERROR] Failed to send alert: {e}")
        db.rollback()
    finally:
        db.close()
