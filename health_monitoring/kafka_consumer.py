# health_monitoring/kafka_consumer.py
# 근로자 센서 이벤트 수신

import json
from kafka import KafkaConsumer
from utils.model_utils import predict_worker_risk
from kafka_producer import send_alert_event
from db.orm_models import Incident
from sqlalchemy.orm import Session

# Kafka 메시지를 받아 건강 이상 여부를 판단하고 DB에 기록 및 결과 발행 함수
def process_message(data: dict, db: Session):
    if data.get("eventType") != "WORKER_VITAL_SIGNS_UPDATED":   # 해당 메시지만 처리
        return
    
    # 센서 데이터 추출
    worker_id = data.get("workerId")
    latitude = data.get("workerLatitude")
    longitude = data.get("workerLongitude")
    age = data.get("age")
    heart_rate = data.get("heartRate")

    # 건강 이상 예측
    print(f"{worker_id} 근로자 건강 이상 예측 시작!")
    result = predict_worker_risk(age, heart_rate)
    print(f"예측 완료: {'위험' if result else '정상'}")

print("\n\n***실시간 근로자 건강 이상 예측 AI 서비스 시작***\n\n")

# KafkaConsumer 정의
consumer = KafkaConsumer(
    "iroom",
    bootstrap_servers=["localhost:9092"],
    auto_offset_reset="latest",
    enable_auto_commit=True,
    group_id="health-service"
)

for message in consumer:
    # JSON 유효성 검사
    try:
        raw = message.value.decode("utf-8") # JSON 파싱
        data = json.loads(raw)  # JSON 변환
    except json.JSONDecodeError as e:
        print("JSON 파싱 오류:", e)
        print("잘못된 메시지 형식입니다.")
        continue

    if data.get("eventType") == "WORKER_VITAL_SIGNS_UPDATED":
        print(f"근로자 센서 데이터 수신 완료!: {data}")

        """
        수신 메시지 예시
        {
            "eventType": "WORKER_VITAL_SIGNS_UPDATED",
            "workerId": "W001",
            "age": 42,
            "heartRate": 96,
            "stepPerMinute": 88,
            "speed": 1.2,
            "pace": 4.5,
            "steps": 120
        }
        """

        # 필드 추출 방법
        # worker_id = data.get("workerId")
        # age = data.get("age")
        # heart_rate = data.get("heartRate")
        # spm = data.get("stepPerMinute")
        # speed = data.get("speed")
        # pace = data.get("pace")
        # steps = data.get("steps")

        worker_id = data.get("workerId")
        latitude = data.get("workerLatitude")
        longitude = data.get("workerLongitude")
        age = data.get("age")
        heart_rate = data.get("heartRate")

        # 근로자 건강 이상 예측
        print(f"{worker_id} 근로자 건강 이상 예측 시작!")
        result = predict_worker_risk(age, heart_rate)

        print(f"근로자 건강 이상 예측 완료!")
        print(f"근로자 건강 상태: {'위험' if result == 1 else '정상'}")

        # 결과 이벤트 발행
        send_alert_event(worker_id, latitude, longitude, result)