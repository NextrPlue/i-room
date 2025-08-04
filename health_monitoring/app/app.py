# health_monitoring/app/main.py
# FastAPI 엔트리포인트
# uvicorn health_monitoring.app.main:app --reload

from fastapi import FastAPI

app = FastAPI()

@app.get("/health/")
async def root():
    return {"message": "FastAPI Health Monitoring"}