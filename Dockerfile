# 1. Python 3.10 이미지 사용 (YOLO는 3.8~3.11까지 호환)
FROM python:3.10-slim

# 2. 작업 디렉토리 생성
WORKDIR /app

# 3. 필요 파일 복사
COPY requirements.txt .

# 4. OpenCV, YOLO 설치 (opencv-python-headless 사용)
RUN apt-get update && apt-get install -y libgl1 libglib2.0-0 && \
    pip install --no-cache-dir -r requirements.txt

# 5. 앱 코드와 모델 복사
COPY safetyGear/ ./safetyGear/

# 6. FastAPI 서버 실행 (uvicorn)
CMD ["uvicorn", "safetyGear.main_video:app", "--host", "0.0.0.0", "--port", "8000"]