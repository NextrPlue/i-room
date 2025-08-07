# health/kafka_consumer.py
# 근로자 센서 이벤트 수신

import json
from kafka import KafkaConsumer

from health.utils.model_utils import predict_worker_risk
from health.utils.db import SessionLocal
from health.db.orm_models import Incident
from health.kafka_producer import send_alert_event

from sqlalchemy.orm import Session
from datetime import datetime
import threading

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

    # 예측 완료 시간 정의
    occurred_at = datetime.now()

    # incidentType, description 정의
    if result == 1:
        incident_type = "이상"
        description = "건강 이상 상태가 감지되었습니다."
    else:
        incident_type = "정상"
        description = "건강 상태는 정상입니다."

    # DB 저장
    new_incident = Incident(
        workerId=worker_id,
        workerLatitude=latitude,
        workerLongitude=longitude,
        incidentType=incident_type,
        incidentDescription=description,
        occurredAt=occurred_at
    )
    db.add(new_incident)
    db.commit()
    db.refresh(new_incident)    # 자동 생성된 incidentId를 다시 불러옴

    # 결과 Kafka 전송
    send_alert_event(new_incident)

def consume_worker_data():
    def run():
        # KafkaConsumer 정의
        consumer = KafkaConsumer(
            "iroom",                                # 센서 이벤트가 발행되는 토픽
            bootstrap_servers=['localhost:9092'],   # Kafka 브로커 주소
            auto_offset_reset="latest",             # 최신 메시지부터 소비
            enable_auto_commit=True,                
            group_id="health-service"               # Cousumer 그룹 ID
        )

        db = SessionLocal()

        for message in consumer:
            try:
                raw = message.value.decode("utf-8") # JSON 파싱
                data = json.loads(raw)  # JSON 변환
                process_message(data, db)
            except Exception as e:
                print("Kafka 메시지 처리 중 오류:", e)

    # 백그라운드 스레드 실행
    # FastAPI가 실행되면 자동으로 Kafka를 소비하기 시작
    thread = threading.Thread(target=run, daemon=True)
    thread.start()