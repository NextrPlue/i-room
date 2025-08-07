# health_monitoring/kafka_producer.py
# 근로자 건강 이상 분류 결과를 Kafka 토픽으로 발행

from kafka import KafkaProducer
import json
from health.db.orm_models import Incident

producer = KafkaProducer(
    bootstrap_servers=["localhost:9092"],
    value_serializer=lambda v: json.dumps(v).encode("utf-8")
)

def send_alert_event(incident: Incident):
    event = {
        "eventType": "HEALTH_ANOMALY",
        "incidentId": incident.incidentId,  # DB에서 생성된 값을 사용
        "workerId": incident.workerId,
        "workerLatitude": incident.workerLatitude,
        "workerLongitude": incident.workerLongitude,
        "incidentType": incident.incidentType,
        "incidentDescription": incident.incidentDescription,
        "occurredAt": incident.occurredAt.strftime("%Y-%m-%d %H:%M:%S")
    }

    producer.send("iroom", event)   # iroom 토픽으로 이벤트 발행
    print(f"예측 결과 전송 완료!: {event}")