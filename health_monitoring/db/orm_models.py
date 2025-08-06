# /db/orm_models.py

from sqlalchemy import Column, String, Float, DateTime
from sqlalchemy.ext.declarative import declarative_base

# 모든 ORM 모델의 부모 클래스
# SQLAlchemy에서 Base.metadata.create_all()을 호출하면
# Base를 상송한 모든 클래스가 DB 테이블로 만들어짐.
Base = declarative_base()

class Incident(Base):
    __tablename__ = "incidents" # DB에 incidents 테이블 생성

    # 컬럼 정의 -> DB 필드에 매핑
    incidentId = Column(String, primary_key=True)   # 고유 위험 ID
    workerId = Column(String)                       # 근로자 ID (kafka로 받음)
    workerLatitude = Column(Float)                  # 근로자 위치 위도 (kafka로 받음)
    workerLongitude = Column(Float)                 # 근로자 위치 경도 (kafka로 받음)
    incidentType = Column(String)                   # 위험 정도 (정상 or 이상)
    incidentDescription = Column(String)            # 상세 설명
    occurredAt = Column(DateTime)                   # 위험 예측 시간