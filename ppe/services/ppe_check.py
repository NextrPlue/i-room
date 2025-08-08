from ppe.orm.watch import Watch  # GPS 테이블 ORM
from sqlalchemy.orm import Session
from ppe.database import SessionLocal


def check_violation(helmet_count: int, seatbelt_count: int) -> dict:
    """
    현재 DB 기준 GPS 등록된 근로자 수(total_workers)를 기준으로
    helmet/seatbelt 착용자 수와 비교하여 미착용 여부 판단
    """
    db: Session = SessionLocal()

    try:
        # GPS 기반 등록된 근로자 수 조회
        total_workers = db.query(Watch.worker_id).distinct().count()

        if total_workers == 0:
            print("[WARNING] GPS 기반 근로자 데이터(watch 테이블)가 존재하지 않습니다.")

        # 가장 낮은 착용 수를 기준으로 비교
        detected_with_ppe = min(helmet_count, seatbelt_count)
        violation = total_workers > detected_with_ppe
        reason = []

        if violation:
            reason.append(
                f"GPS 등록된 총 근로자 수={total_workers}, PPE 착용 인원={detected_with_ppe}"
            )

        return {
            "helmet_on": helmet_count,
            "seatbelt_on": seatbelt_count,
            "total_workers": total_workers,
            "violation": violation,
            "reason": reason,
        }
    finally:
        db.close()
