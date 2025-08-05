# health_monitoring/kafka_producer.py
# 근로자 건강 이상 분류 결과를 Kafka 토픽으로 발행

from kafka import KafkaProducer
import json
import uuid
from datetime import datetime

producer = KafkaProducer(
    bootstrap_servers=["localhost:9092"],
    value_serializer=lambda v: json.dumps(v).encode("utf-8")
)

def send_alert_event(worker_id, latitude, longitude, risk_level):
    # incidentType과 description 정의
    if risk_level == 1:
        incident_type = "이상"
        description = "건강 이상 상태가 감지되었습니다."
    else:
        incident_type = "정상"
        description = "건강 상태는 정상입니다."

    event = {
        "eventType": "HEALTH_ANOMALY",
        "workerId": worker_id,
        "workerLatitude": latitude,
        "workerLongitude": longitude,
        "incidentId": str(uuid.uuid4()), # 고유 이벤트 ID
        "occurredAt": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "incidentType": incident_type,
        "incidentDescription": description
    }

    producer.send("iroom", event)   # iroom 토픽으로 이벤트 발행
    print(f"예측 결과 전송 완료!: {event}")