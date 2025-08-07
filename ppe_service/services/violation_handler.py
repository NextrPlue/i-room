from sqlalchemy.orm import Session
from database import SessionLocal
from orm.ppe import PPEDetection
from orm.violation import Violation
from orm.watch import Watch
from utils.capture_util import save_capture
from utils.alert_service import send_alert_if_violation

def handle_violation(helmet_count, seatbelt_count, frame, violation_info, interval_sec=10):
    """
    위반 발생 시 DB insert, 캡처 저장, 알림 전송
    """
    db: Session = SessionLocal()
    try:
        # PPE Detection 로그 저장 (모든 집계 결과 기록)
        detection_record = PPEDetection(
            ppe_id=None,
            imageURL=None,  # violation 발생 시에만 이미지 저장
            helmet_on=helmet_count,
            seatbelt_on=seatbelt_count
        )
        db.add(detection_record)
        db.commit()
        db.refresh(detection_record)

        if violation_info["violation"]:
            # 최신 GPS 정보 가져오기
            latest_watch = (
                db.query(Watch)
                .order_by(Watch.timestamp.desc())
                .first()
            )

            if latest_watch:
                # 캡처 이미지 저장
                img_url = save_capture(frame)

                # violation 테이블에 저장
                violation_record = Violation(
                    worker_id=latest_watch.worker_id,
                    ppe_id=detection_record.id,
                    imageURL=img_url,
                    latitude=latest_watch.latitude,
                    longitude=latest_watch.longitude,
                    timestamp=latest_watch.timestamp,
                    helmet_on=helmet_count,
                    seatbelt_on=seatbelt_count
                )
                db.add(violation_record)
                db.commit()

                # 알림 전송
                send_alert_if_violation(violation_record.id, frame)

    except Exception as e:
        print(f"[ERROR] Violation handling failed: {e}")
        db.rollback()
    finally:
        db.close()
