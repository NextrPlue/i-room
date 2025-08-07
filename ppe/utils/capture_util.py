import os
import cv2
from datetime import datetime

# FastAPI static 경로 기준
BASE_DIR = "static/alerts"
BASE_URL = "/static/alerts"

def save_capture(frame):
    """
    프레임을 캡처해서 static/alerts에 저장 후, 접근 가능한 URL 반환
    """
    if not os.path.exists(BASE_DIR):
        os.makedirs(BASE_DIR)

    filename = datetime.utcnow().strftime("%Y%m%d_%H%M%S") + ".jpg"
    filepath = os.path.join(BASE_DIR, filename)

    # 이미지 저장
    cv2.imwrite(filepath, frame)

    # URL 반환
    image_url = f"{BASE_URL}/{filename}"
    return image_url
