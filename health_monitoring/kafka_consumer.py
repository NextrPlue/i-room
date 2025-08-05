# health_monitoring/kafka_consumer.py
# 근로자 센서 이벤트 수신

import json
from kafka import KafkaConsumer
from utils.model_utils import predict_worker_risk
from kafka_producer import send_alert_event

print("\n\n***실시간 근로자 건강 이상 예측 AI 서비스 시작***\n\n")

# KafkaConsumer 정의
consumer = KafkaConsumer(
    "iroom",
    bootstrap_servers=["localhost:9092"],
    auto_offset_reset="latest",
    enable_auto_commit=True,
    group_id="health-service",
    value_deserializer=lambda m: json.loads(m.decode("utf-8"))
)

for message in consumer:
    data = message.value
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
        send_alert_event(worker_id, result)