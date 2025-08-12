from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from ppe.database import get_db
from ppe.orm.watch import Watch
from ppe.schemas.watch_schema import WatchCreate
from datetime import datetime, timedelta

router = APIRouter(prefix="/gps", tags=["GPS"])

@router.post("/")
def receive_gps(data: WatchCreate, db: Session = Depends(get_db)):
    new_data = Watch(
        worker_id=data.workerId,
        latitude=data.latitude,
        longitude=data.longitude,
        timestamp=data.timestamp or datetime.utcnow() + timedelta(hours=9)  # 요청에 없으면 현재 시간
    )
    db.add(new_data)
    db.commit()
    db.refresh(new_data)
    return {"msg": "GPS 저장 완료", "id": new_data.id}
