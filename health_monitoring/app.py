# health_monitoring/app.py
# uvicorn health_monitoring.app:app --reload

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import pandas as pd
import os
import joblib
from utils.db import init_db, get_session
from kafka_consumer import consume_worker_data
from contextlib import asynccontextmanager
from utils.schemas import IncidentResponse
from db.orm_models import Incident
from sqlalchemy.orm import Session

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("DB 초기화")
    init_db()

    print("Kafka Consumer 시작")
    consume_worker_data()   # Kafka 소비 시작 (스레드로 작동)

    yield   # 앱 실행 시작

    # 종료 시 로직이 있다면 여기 작성
    print("서버 종료 중..")

app = FastAPI(lifespan=lifespan) # FastAPI 초기화, lifespan 방식

@app.get("/")
def root():
    return {"message": "근로자 건강 이상 예측 시스템 실행 중!"}

@app.get("/incident/{incident_id}", response_model=IncidentResponse)
def get_incident_by_id(incident_id: str, db: Session = get_session()):
    incident = db.query(Incident).filter(Incident.incidentId == incident_id).first()
    if not incident:
        raise HTTPException(status_code=404, detail="해당 incidentId를 찾을 수 없습니다.")
    return incident