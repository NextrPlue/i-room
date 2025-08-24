# routers/detect_router.py
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from ppe.database import SessionLocal
from ppe.schemas import ppe_schema

# 기존 조회/등록 라우터 (그대로 유지)
router = APIRouter(prefix="/detection", tags=["PPE Detection"])

# def get_db():
#     db = SessionLocal()
#     try:
#         yield db
#     finally:
#         db.close()

# @router.get("/latest", response_model=ppe_schema.PPEDetectionResponse)
# def get_latest_detection(db: Session = Depends(get_db)):
#     record = db.query(PPEDetection).order_by(PPEDetection.timestamp.desc()).first()
#     if not record:
#         raise HTTPException(status_code=404, detail="No detection record found")
#     return record

# @router.get("/", response_model=list[ppe_schema.PPEDetectionResponse])
# def get_all_detections(db: Session = Depends(get_db)):
#     return db.query(PPEDetection).order_by(PPEDetection.timestamp.desc()).all()

# @router.post("/", response_model=ppe_schema.PPEDetectionResponse)
# def create_detection(data: ppe_schema.PPEDetectionCreate, db: Session = Depends(get_db)):
#     record = PPEDetection(
#         workerId=data.workerId,
#         ppe_id=data.ppe_id,
#         imageURL=data.imageURL,
#         confidenceScore=data.confidenceScore,
#         helmet_on=data.helmet_on,
#         seatbelt_on=data.seatbelt_on,
#     )
#     db.add(record)
#     db.commit()
#     db.refresh(record)
#     return record


# 추론 시작/중지 컨트롤 라우터
from ppe.services.yolo_service import start_detection, stop_detection

control = APIRouter(prefix="/detect", tags=["Detection Control"])

@control.post("/start")
async def detect_start(loop_file: bool = False):
    """YOLO 추론 시작 (이미 실행 중이면 started=false 반환)"""
    started = await start_detection(loop_file=loop_file)
    return {"started": started}

@control.post("/stop")
def detect_stop():
    """YOLO 추론 정지 플래그 설정"""
    stopped = stop_detection()
    return {"stopped": stopped}

@control.get("/status")
def detect_status():
    """간단한 상태 확인용 (원하면 yolo_service에 상태값 노출해서 고도화 가능)"""
    # (필요하면) yolo_service에 현재 실행 여부/소스 등을 저장해 반환하도록 확장
    return {"ok": True}
