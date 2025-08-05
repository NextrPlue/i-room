# health_monitoring/kafka_producer.py
# 근로자 건강 이상 분류 결과를 Kafka 토픽으로 발행

from kafka import KafkaProducer
import json

producer = KafkaProducer(
    bootstrap_server=["localhost:9092"],
    value_serializer=lambda v: json.dumps(v).encode("utf-8")
)

def send_alert_event(worker_id, risk_level):
    event = {
        "eventType": "WORKER_HEALTH_ALERT",
        "workerId": worker_id,
        "riskLevel": risk_level # 0=정상, 1=위험
    }

    producer.send("worker_alerts", event)
    print(f"예측 결과 전송 완료!: {event}")