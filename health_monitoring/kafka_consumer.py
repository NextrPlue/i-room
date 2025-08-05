# health_monitoring/kafka_consumer.py
# 근로자 센서 이벤트 수신

import json
import threading
from kafka import KafkaConsumer, KafkaProducer

consumer = KafkaConsumer(
    "iroom",
    bootstrap_servers=["kafka:9093"],
    auto_offset_reset="latest",
    enable_auto_commit=True,
    group_id="health-service",
    value_deserializer=lambda m: json.loads(m.decode("utf-8"))
)