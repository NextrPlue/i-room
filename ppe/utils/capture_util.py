# ppe/utils/capture_util.py
import os
import cv2
from uuid import uuid4
from datetime import datetime
from pathlib import Path
from urllib.parse import urljoin

PUBLIC_BASE_URL   = os.getenv("PUBLIC_BASE_URL", "http://127.0.0.1:8000")
STATIC_DIR        = "ppe/static/alerts"        # 실제 저장 경로
STATIC_URL_PREFIX = "/static/alerts"           # FastAPI에서 static mount 기준

def save_capture(frame) -> str:
    """
    프레임을 ppe/static/alerts에 저장하고 외부 접근 가능한 절대 URL 반환
    """
    save_dir = Path(STATIC_DIR)
    save_dir.mkdir(parents=True, exist_ok=True)  # 폴더 없으면 생성

    ts = datetime.utcnow().strftime("%Y%m%d_%H%M%S")
    filename = f"{ts}_{uuid4().hex[:8]}.jpg"
    filepath = save_dir / filename

    ok = cv2.imwrite(str(filepath), frame)
    if not ok:
        raise RuntimeError(f"Failed to save image at {filepath}")

    # URL 생성
    relative_url = f"{STATIC_URL_PREFIX.rstrip('/')}/{filename}"
    absolute_url = urljoin(PUBLIC_BASE_URL.rstrip('/') + '/', relative_url.lstrip('/'))
    return absolute_url
