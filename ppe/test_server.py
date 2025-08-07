from fastapi import FastAPI, Request
from datetime import datetime
import os
import json

app = FastAPI()

SAVE_LOG = "received_logs"
os.makedirs(SAVE_LOG, exist_ok=True)

@app.post("/api/alerts")
async def receive_alert(request: Request):
    data = await request.json()
    helmet_count = data.get("helmet_count")
    seatbelt_count = data.get("seatbelt_count")
    timestamp = data.get("timestamp")
    image_url = data.get("image_url")

    print("=== [테스트 서버] 경고 수신 ===")
    print(f"탐지 헬멧 수: {helmet_count}")
    print(f"탐지 안전벨트 수: {seatbelt_count}")
    print(f"발생 시간: {timestamp}")
    print(f"이미지 URL: {image_url}")
    print("============================")

    # 로그 파일에 기록
    log_filename = os.path.join(SAVE_LOG, "alerts_log.txt")
    with open(log_filename, "a", encoding="utf-8") as f:
        f.write(json.dumps(data, ensure_ascii=False) + "\n")

    return {
        "status": "ok",
        "received": data
    }
