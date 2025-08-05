# health_monitoring/kafka_consumer.py
# 근로자 센서 이벤트 수신

import json
import threading
from kafka import KafkaConsumer, KafkaProducer

# KafkaConsumer 정의
consumer = KafkaConsumer(
    "iroom",
    bootstrap_servers=["kafka:9093"],
    auto_offset_reset="latest",
    enable_auto_commit=True,
    group_id="health-service",
    value_deserializer=lambda m: json.loads(m.decode("utf-8"))
)

for message in consumer:
    data = message.value
    if data.get("eventType") == "WORKER_VITAL_SIGNS_UPDATED":
        print(f"근로자 센서 데이터 수신: {data}")

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