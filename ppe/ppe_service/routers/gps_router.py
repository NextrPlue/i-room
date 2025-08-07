from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
from database import get_db
from orm.watch import Watch

router = APIRouter(prefix="/gps", tags=["GPS"])

@router.post("/")  # api 요청 형태 변환 필요
def receive_gps(workerId: int, latitude: float, longitude: float, db: Session = Depends(get_db)):
    new_data = Watch(workerId=workerId, latitude=latitude, longitude=longitude)
    db.add(new_data)
    db.commit()
    db.refresh(new_data)
    return {"msg": "GPS 저장 완료", "id": new_data.id}
