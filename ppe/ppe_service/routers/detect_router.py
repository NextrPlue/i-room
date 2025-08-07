# routers/detect_router.py
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from database import SessionLocal
from orm.ppe import PPEDetection
from schemas import ppe_schema

router = APIRouter(prefix="/detection", tags=["PPE Detection"])

# DB 세션 의존성 주입
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# 최근 탐지 결과 조회
@router.get("/latest", response_model=ppe_schema.PPEDetectionResponse)
def get_latest_detection(db: Session = Depends(get_db)):
    record = db.query(PPEDetection).order_by(PPEDetection.timestamp.desc()).first()
    if not record:
        raise HTTPException(status_code=404, detail="No detection record found")
    return record

# 모든 탐지 결과 조회
@router.get("/", response_model=list[ppe_schema.PPEDetectionResponse])
def get_all_detections(db: Session = Depends(get_db)):
    return db.query(PPEDetection).order_by(PPEDetection.timestamp.desc()).all()

# 새로운 탐지 결과 수동 추가 (테스트용)
@router.post("/", response_model=ppe_schema.PPEDetectionResponse)
def create_detection(data: ppe_schema.PPEDetectionCreate, db: Session = Depends(get_db)):
    record = PPEDetection(
        workerId=data.workerId,
        ppe_id=data.ppe_id,
        imageURL=data.imageURL,
        confidenceScore=data.confidenceScore,
        helmet_on=data.helmet_on,
        seatbelt_on=data.seatbelt_on,
    )
    db.add(record)
    db.commit()
    db.refresh(record)
    return record
