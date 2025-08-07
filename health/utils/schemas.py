# health/utils/schemas.py

from pydantic import BaseModel
from datetime import datetime

# 클라이언트에게 주는 응답 스키마
# FastAPI에서 이 스키마를 기반으로 자동으로 응답 JSON을 생성
# Swagger 문서에 자동 등록
# 클라이언트가 확인 가능한 공개 정보
class IncidentResponse(BaseModel):
    incidentId: int
    workerId: int
    workerLatitude: float
    workerLongitude: float
    incidentType: str
    incidentDescription: str
    occurredAt: datetime

    class Config:
        from_attributes = True # ORM 객체를 Pydantic 모델로 자동 변환