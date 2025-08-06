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

# === 입력 스키마 정의 ===
class HealthInput(BaseModel):
    age: int
    heart_rate: float

# === 모델 로드 ===
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "models", "lgb_model_2.pkl")

with open(MODEL_PATH, "rb") as f:
    model = joblib.load(f)

# === 기본 엔드포인트 ===
@app.get("/health")
async def root():
    return {"message": "FastAPI Health Monitoring"}

# === 예측 엔드포인트 ===
@app.post("/health/predict")
def predict_health_risk(data: HealthInput):
    age = data.age
    hr = data.heart_rate
    hr_max = 220 - age
    hr_ratio = hr / hr_max

    X_input = pd.DataFrame([[age, hr, hr_max, hr_ratio]],
                           columns=["Age", "HR", "HRmax", "hr_ratio"])

    prediction = model.predict(X_input)[0]
    result = "위험" if prediction == 1 else "정상"

    return {"예측 결과": result, "code": int(prediction)}