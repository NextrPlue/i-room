# health/kafka_consumer.py
# 근로자 센서 이벤트 수신

import json
from kafka import KafkaConsumer

from health.utils.model_utils import predict_worker_risk
from health.utils.db import SessionLocal
from health.db.orm_models import Incident
from health.kafka_producer import send_alert_event

from sqlalchemy.orm import Session
from datetime import datetime, timezone, timedelta
import threading

KST = timezone(timedelta(hours=9))  # 대한민국 시간 설정

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

    # 값이 누락된 경우 종료
    if worker_id is None or age is None or heart_rate is None:
        print(f"[WARN] 필수 값 누락(workerId/age/heartRate): {data}")
        return

    # 건강 이상 예측
    print(f"[INFO] {worker_id} 근로자 건강 이상 예측 시작... age={age}, HR={heart_rate}")
    try:
        res = predict_worker_risk(age, heart_rate)
        if not isinstance(res, dict):
            raise ValueError(f"predict_worker_risk returned non-dict: {res}")
    except Exception as e:
        print("[ERROR] 예측 실패:", e)
        return
    
    # 예측 결과 파싱
    is_risk = int(res.get("pred", 0)) == 1
    proba = float(res.get("proba", 0.0))
    threshold = float(res.get("threshold", 0.5))
    model_type = res.get("model_type", "unknown")

    print(f"[INFO] 예측 완료: {'위험' if is_risk else '정상'} | "
          f"proba={proba:.3f} (th={threshold:.3f}, model={model_type})")

    # 예측 완료 시간 정의
    occurred_at = datetime.now(KST)

    # incidentType, description 정의
    incident_type = "이상" if is_risk else "정상"
    description = (
        "건강 이상 상태가 감지되었습니다." if is_risk else "건강 상태는 정상입니다."
    )

    # DB 저장 (스키마에 따라 선택적으로 확장)
    incident_kwargs = dict(
        workerId=worker_id,
        workerLatitude=latitude,
        workerLongitude=longitude,
        incidentType=incident_type,
        incidentDescription=description,
        occurredAt=occurred_at
    )

    new_incident = Incident(**incident_kwargs)
    db.add(new_incident)
    db.commit()
    db.refresh(new_incident)    # 자동 생성된 incidentId를 다시 불러옴

    # 결과 Kafka 전송
    try:
        send_alert_event(new_incident)
    except Exception as e:
        print("[WARN] 경보 이벤트 송신 실패:", e)

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