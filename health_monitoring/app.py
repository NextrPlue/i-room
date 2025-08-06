# health_monitoring/app.py
# uvicorn health_monitoring.app:app --reload

from fastapi import FastAPI
from pydantic import BaseModel
import pandas as pd
import os
import joblib
from utils.db import init_db, get_session
from kafka_consumer import consume_worker_data
from contextlib import asynccontextmanager

app = FastAPI() # FastAPI 초기화

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("DB 초기화")
    init_db()

    print("Kafka Consumer 시작")
    consume_worker_data()   # Kafka 소비 시작 (스레드로 작동)

    yield   # 앱 실행 시작

    # 종료 시 로직이 있다면 여기 작성
    print("서버 종료 중..")