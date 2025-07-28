# safetyGear/dummy_server.py
from fastapi import FastAPI, UploadFile, File, Form
from datetime import datetime
import os

app = FastAPI()

SAVE_DIR = "recieved_images"
os.makedirs(SAVE_DIR, exist_ok=True)

@app.post("/api/alerts")
async def receive_alert(
    missing_items: str = Form(...),
    timestamp: str = Form(...),
    image: UploadFile = File(...)
):
    print("=== [테스트 서버] 경고 수신 ===")
    print(f"누락 보호구: {missing_items}")
    print(f"발생 시간: {timestamp}")
    print(f"이미지 파일명: {image.filename}")
    print("============================")

    # 이미지 저장
    contents = await image.read()
    filename = f"{timestamp.replace(':', '-')}_{image.filename}"
    save_path = os.path.join(SAVE_DIR, filename)

    with open(save_path, "wb") as f:
        f.write(contents)
    print(f"[테스트 서버] 이미지 저장 완료: {save_path}")

    return {
        "status": "ok",
        "received": {
            "missing_items": missing_items,
            "timestamp": timestamp,
            "image_filename": image.filename
        }
    }