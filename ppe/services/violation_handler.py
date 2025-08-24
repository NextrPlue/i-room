from sqlalchemy.orm import Session
from ppe.database import SessionLocal
from ppe.orm.violation import Violation
from ppe.orm.watch import Watch
from ppe.utils.capture_util import save_capture
from ppe.utils.alert_service import send_alert_if_violation

# 위반 발생 시 DB insert, 캡처 저장, 알림 전송
def handle_violation(frame, violation_info):
    interval_sec=10
    type = '보호구 미착용'
    
    db: Session = SessionLocal()
    try:
        # # PPE Detection 로그 저장 
        # detection_record = PPEDetection(
        #     # ppe_id=None,              # 추후 필요 시 수정
        #     image_url=None,           # PPE 테이블에서는 이미지 없이도 OK
        #     helmet_on_count=helmet_count,
        #     seatbelt_on_count=seatbelt_count
        # )
        # db.add(detection_record)
        # db.commit()
        # db.refresh(detection_record)

        # 위반 발생 시에만 Violation 테이블에 저장
        if violation_info["violation"]:
            # 최신 GPS 정보 가져오기
            latest_watch = (
                db.query(Watch)
                .order_by(Watch.timestamp.desc())
                .first()
            )

            if latest_watch:
                # 이미지 캡처 및 저장
                img_url = save_capture(frame)

                # Violation 테이블에 기록
                violation_record = Violation(
                    worker_id=latest_watch.worker_id,
                    # ppe_id=detection_record.id,            # 감지 기록과 연결               
                    timestamp=latest_watch.timestamp,  
                    incident_type = type,
                    latitude=latest_watch.latitude,
                    longitude=latest_watch.longitude,  
                    # helmet_on_count=helmet_count,
                    # seatbelt_on_count=seatbelt_count
                    incident_description = violation_info["reason"],  # 테스트
                    image_url=img_url,  
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
