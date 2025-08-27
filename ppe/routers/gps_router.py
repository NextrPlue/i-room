from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from typing import List
from ppe.database import get_db
from ppe.orm.watch import Watch
from ppe.schemas.watch_schema import WatchCreate
from datetime import datetime, timedelta

router = APIRouter(prefix="/gps", tags=["GPS"])

# 스냅샷 반영 -> 들어온 workerId 는 업서트 Update 하거나 Insert 하거나 , 스냅샷에 없는 workerId는 삭제 하는 식으로 갱신
@router.post("/")
def receive_gps(data: List[WatchCreate], db: Session = Depends(get_db)):
    # 스냅샷 시각(KST)
    now_kst = datetime.utcnow() + timedelta(hours=9)

    # 이번 배치에 포함된 workerId 집합
    incoming_ids = {d.workerId for d in data}

    # 미리 기존 레코드 로드 (이번 배치에 해당하는 것만)
    if incoming_ids:
        existing_rows = db.query(Watch).filter(Watch.worker_id.in_(incoming_ids)).all()
    else:
        existing_rows = []

    existing_map = {row.worker_id: row for row in existing_rows}

    inserted = 0
    updated = 0

    # 업서트 처리
    for d in data:
        ts = d.timestamp or now_kst
        w = existing_map.get(d.workerId)
        if w:
            # UPDATE
            w.latitude = d.latitude
            w.longitude = d.longitude
            w.timestamp = ts
            updated += 1
        else:
            # INSERT
            db.add(Watch(
                worker_id=d.workerId,
                latitude=d.latitude,
                longitude=d.longitude,
                timestamp=ts
            ))
            inserted += 1

    # 스냅샷에 없는 workerId는 삭제
    # (스냅샷이 비어 있으면 전체 삭제)
    deleted = db.query(Watch).filter(~Watch.worker_id.in_(incoming_ids) if incoming_ids else True).delete(synchronize_session=False)

    db.commit()

    return {
        "msg": "스냅샷 반영 완료",
        "inserted": inserted,
        "updated": updated,
        "deleted": deleted,
        "active_workers": len(incoming_ids)
    }
