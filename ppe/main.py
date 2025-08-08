from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from pathlib import Path
from ppe.routers import gps_router, detect_router, monitor_router
from ppe.database import Base, engine

import threading
import webbrowser
import time

BASE_DIR = Path(__file__).resolve().parent
STATIC_DIR = BASE_DIR / "static"
STATIC_DIR.mkdir(parents=True, exist_ok=True)

async def lifespan(app: FastAPI):
    print("PPE Start")
    # DB 생성
    Base.metadata.create_all(bind=engine)

    # 서버 시작 후 브라우저 열기, 테스트시 주석처리하세요
    def open_browser():
        time.sleep(1)
        webbrowser.open("http://127.0.0.1:8000/monitor")
    threading.Thread(target=open_browser).start()

    yield
    print("PPE Fin")

app = FastAPI(lifespan=lifespan)

app.include_router(gps_router.router)
app.include_router(detect_router.router)   
app.include_router(detect_router.control)  
app.include_router(monitor_router.router)

app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")
