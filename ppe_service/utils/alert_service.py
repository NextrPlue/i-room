import requests
import os
from datetime import datetime, timedelta
import cv2
from sqlalchemy.orm import Session
from database import SessionLocal
from orm.ppe import PPEDetection
from orm.watch import Watch

# Spring Boot 서버 API 주소
SPRING_BOOT_API = "http://localhost:8080/api/alerts"

# 정적 파일 저장 경로
STATIC_DIR = os.path.join(os.path.dirname(__file__), "static", "alerts")
os.makedirs(STATIC_DIR, exist_ok=True)

def send_alert_if_violation(record_id: int, frame, interval_sec: int = 10):
    """
    DB에서 탐지 결과와 watch 테이블의 workerId를 비교하여 미착용이면 Spring Boot 서버로 Alert 전송
    """
    db: Session = SessionLocal()
    try:
        # 1. 방금 저장된 탐지 기록 조회
        detection = db.query(PPEDetection).filter(PPEDetection.id == record_id).first()
        if not detection:
            print(f"[WARN] Detection record not found (id={record_id})")
            return

        # 2. 최근 interval 동안의 workerId 수 조회
        since_time = datetime.utcnow() - timedelta(seconds=interval_sec)
        worker_ids = (
            db.query(Watch.workerId)
            .filter(Watch.timestamp >= since_time)
            .distinct()
            .all()
        )
        total_workers = len(worker_ids)

        # 3. PPE 착용자 수 추정 (둘 다 착용으로 인정)
        detected_with_ppe = min(detection.helmet_on, detection.seatbelt_on)

        # 4. 미착용 여부 판단
        violation = total_workers > detected_with_ppe
        if not violation:
            print("[INFO] No violation → alert skipped")
            return

        # 5. 캡쳐 이미지 저장
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"{timestamp}.jpg"
        filepath = os.path.join(STATIC_DIR, filename)
        cv2.imwrite(filepath, frame)

        # URL 생성 (FastAPI static 라우터 필요)
        image_url = f"http://127.0.0.1:8000/static/alerts/{filename}"

        # 6. GPS 위치 (workerId 기준 최신)
        gps_data = None
        if detection.workerId:
            gps_data = (
                db.query(Watch)
                .filter(Watch.workerId == detection.workerId)
                .order_by(Watch.timestamp.desc())
                .first()
            )

        # 7. 전송 데이터 구성
        data = {
            "id": detection.id,
            "ppe_id": detection.ppe_id,
            "workerId": detection.workerId,
            "imageURL": image_url,
            "confidenceScore": detection.confidenceScore,
            "workerLocation": {
                "latitude": gps_data.latitude if gps_data else None,
                "longitude": gps_data.longitude if gps_data else None,
            },
            "timestamp": detection.timestamp.strftime("%Y-%m-%d %H:%M:%S"),
            "helmet_on": detection.helmet_on,
            "seatbelt_on": detection.seatbelt_on,
            "total_workers": total_workers,
        }

        # 8. Spring Boot 서버로 전송
        r = requests.post(SPRING_BOOT_API, json=data, timeout=5)
        print(f"[INFO] Alert sent: {r.status_code}, URL: {image_url}")

    except Exception as e:
        print(f"[ERROR] Failed to send alert: {e}")

    finally:
        db.close()
