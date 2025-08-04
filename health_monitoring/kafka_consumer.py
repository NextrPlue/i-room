# health_monitoring/kafka_consumer.py
# 건강 이상 분류 요청 수신

import json
import threading
from kafka import KafkaConsumer, KafkaProducer