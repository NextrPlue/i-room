from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
import asyncio

from routers import gps_router, detect_router, monitor_router
from services.yolo_service import run_detection_loop

# Lifespan: 앱 실행 시 YOLO 루프 실행
async def lifespan(app: FastAPI):
    print("PPE Start")

    # YOLO 탐지 루프 백그라운드로 실행
    asyncio.create_task(run_detection_loop())

    yield

    print("PPE Fin")

# FastAPI 앱 생성
app = FastAPI(lifespan=lifespan)

# 라우터 등록
app.include_router(gps_router.router)
app.include_router(detect_router.router)
app.include_router(monitor_router.router)

# 정적 파일 제공 (/static → static/alerts 폴더 등)
app.mount("/static", StaticFiles(directory="static"), name="static")
