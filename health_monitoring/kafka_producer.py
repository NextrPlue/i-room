# health_monitoring/kafka_producer.py
# 근로자 건강 이상 분류 결과를 Kafka 토픽으로 발행

from kafka import KafkaProducer
import json

producer = KafkaProducer(
    bootstrap_server=["localhost:9092"],
    value_serializer=lambda v: json.dumps(v).encode("utf-8")
)