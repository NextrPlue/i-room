# health/kafka_consumer.py
# 근로자 센서 이벤트 수신

import json
from kafka import KafkaConsumer

from utils.model_utils import predict_worker_risk
from utils.db import SessionLocal
from db.orm_models import Incident
from kafka_producer import send_alert_event

from sqlalchemy.orm import Session
from datetime import datetime, timezone, timedelta
import threading

from utils.rules import apply_rules, CFG, _hrmax, _intensity_score

KST = timezone(timedelta(hours=9))  # 대한민국 시간 설정

def _reason_text(age, hr, spm, speed_kmh, pace_minpkm, reason: str, cfg=CFG) -> str:
    """룰 사유 코드를 사람이 읽기 좋게 변환 (필요 최소 컨텍스트만 계산)"""
    try:
        hrmax = _hrmax(age)
        hr_ratio = (float(hr) / hrmax) if hr is not None else 0.0
        intensity = _intensity_score(spm, speed_kmh, pace_minpkm)
    except Exception:
        hr_ratio, intensity = 0.0, 0.0

    msgs = {
        "model_normal": "모델이 정상으로 판단했습니다.",
        "rule_suppress_low_hr": (
            f"심박이 낮아(HR<{cfg.hr_normal_cap}, HR/HRmax<{cfg.hr_ratio_veto:.2f}) "
            "운동/휴식으로 판단되어 정상으로 판단했습니다."
        ),
        "rule_suppress_active": (
            f"활동 강도 {intensity:.0f}점, 상대심박 {hr_ratio:.2f}로 "
            "활동 대비 과도하지 않아 정상으로 판단했습니다."
        ),
        "rule_suppress_active_strong": (
            f"강한 모델 신호였지만, 활동 강도 {intensity:.0f}점, 상대심박 {hr_ratio:.2f}로 "
            "활동 대비 과도하지 않아 정상으로 판단했습니다."
        ),
        "rule_confirm_high_hr_low_activity": (
            f"저활동(≤{cfg.low_steps_threshold} spm)인데 상대심박 {hr_ratio:.2f}↑ 으로 "
            "이상으로 판단했습니다."
        ),
        "model_confirm": "모델 신호가 강해 이상으로 판단했습니다.",
    }
    return msgs.get(reason, f"규칙 사유: {reason}")

# Kafka 메시지를 받아 건강 이상 여부를 판단하고 DB에 기록 및 결과 발행 함수
def process_message(data: dict, db: Session):
    if data.get("eventType") != "WORKER_SENSOR_UPDATED":   # 해당 메시지만 처리
        return
    
    payload = data.get("data") or {}   # None 방어
    
    # 센서 데이터 추출
    worker_id = payload.get("workerId")
    latitude = payload.get("latitude")
    longitude = payload.get("longitude")
    age = payload.get("age")
    heart_rate = payload.get("heartRate")

    # 운동 데이터도 추출
    steps = payload.get("steps")
    speed = payload.get("speed")
    pace = payload.get("pace")
    spm = payload.get("stepPerMinute")

    # 필수 값 체크
    missing = []
    if worker_id is None: 
        missing.append("workerId")
    if age is None: 
        missing.append("age")
    if heart_rate is None: 
        missing.append("heartRate")

    # 값이 누락된 경우 종료
    if missing:
        print(f"[WARN] 필수 값 누락({', '.join(missing)}): payload={payload}")
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
    model_pred = int(res.get("pred", 0)) == 1
    proba = float(res.get("proba", 0.0))
    threshold = float(res.get("threshold", 0.5))
    model_type = res.get("model_type", "unknown")

    # Rule 레이어 적용 -> 최종 판단, 사유 도출
    final_pred, reason = apply_rules(
        age=age, hr=heart_rate,
        steps_per_minute=spm, speed_kmh=speed, pace_min_per_km=pace,
        model_pred=model_pred, model_proba=proba, threshold=threshold
    )
    is_risk = bool(final_pred)

    # 예측 설명 문장 생성
    reason_text = _reason_text(age, heart_rate, spm, speed, pace, reason, cfg=CFG)

    print(f"[INFO] 예측 완료: {'위험' if is_risk else '정상'} | "
          f"proba={proba:.3f} (th={threshold:.3f}, model={model_type}, reason={reason}) | {reason_text}")

    # 예측 완료 시간 정의
    occurred_at = datetime.now(KST)

    # incidentType, description 정의
    incident_type = "이상" if is_risk else "정상"
    description = (
        "건강 이상 상태가 감지되었습니다." if is_risk else "건강 상태는 정상입니다."
    )
    description += (
        f" (proba={proba:.3f}, th={threshold:.3f}, model={model_type}, reason={reason})"
        f" | {reason_text}"
    )

    # DB 저장 (스키마에 따라 선택적으로 확장)
    incident_kwargs = dict(
        workerId=worker_id,
        latitude=latitude,
        longitude=longitude,
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
            bootstrap_servers=['i-room-kafka:9092'], # Kafka 브로커 주소
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