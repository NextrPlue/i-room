import os, time, json, base64, requests
from sqlalchemy.orm import Session
from ppe.database import SessionLocal
from ppe.orm.violation import Violation
from datetime import datetime, timezone

AUTH_URL = os.getenv("AUTH_URL", "http://135.149.162.178/api/user/systems/authenticate")
API_KEY = os.getenv("apikey", "ppe-system-api-key-cf9e02d6-06ff-4f1d-a4c2-c5acc6cc59df")   # 실제 키/환경변수
ALARM_API = os.getenv("ALARM_API", "http://135.149.162.178/api/alarm/alarms/ppe")

sess = requests.Session()
_token = {"val": None, "exp": 0}

def _jwt_exp(tok: str) -> int:
    try:
        p = tok.split(".")[1] + "=" * (-len(tok.split(".")[1]) % 4)
        return int(json.loads(base64.urlsafe_b64decode(p))["exp"])
    except Exception:
        return 0

def get_token() -> str:
    t_env = os.getenv("SPRING_BOOT_TOKEN")
    if t_env: return t_env
    now = int(time.time())
    if _token["val"] and _token["exp"] - 60 > now:
        return _token["val"]
    r = sess.post(AUTH_URL, json={"apiKey": API_KEY}, timeout=5)
    r.raise_for_status()
    tok = r.json()["data"]["token"]
    _token.update({"val": tok, "exp": _jwt_exp(tok) or now + 600})
    return tok

def _fmt_occurred_at_iso(dt: datetime) -> str:
    # 서버(LocalDateTime)가 오프셋 없는 ISO-8601을 기대한다고 가정
    if dt.tzinfo:
        dt = dt.astimezone(None).replace(tzinfo=None)
    return dt.strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3]  # 밀리초(3자리)

def send_alert_if_violation(violation_id: int, frame=None):
    db: Session = SessionLocal()
    try:
        v = db.query(Violation).filter(Violation.id == violation_id).first()
        if not v:
            print(f"[WARN] no violation id={violation_id}")
            return

        base_dt = v.timestamp or datetime.utcnow()
        occurred_at = _fmt_occurred_at_iso(base_dt)

        payload = {
            "workerId": v.worker_id,
            "occurredAt": occurred_at, 
            "incidentType": v.incident_type,  
            "incidentId": v.id,  
            "workerLatitude": v.latitude,
            "workerLongitude": v.longitude,
            "incidentDescription": v.incident_description,
            "workerImageUrl": getattr(v, "image_url", None),
        }

        payload = {k: val for k, val in payload.items() if val is not None}

        headers = {
            # "Content-Type": "application/json; charset=utf-8",
            # "Accept": "application/json",
            "Authorization": f"Bearer {get_token()}",
        }

        resp = sess.post(ALARM_API, json=payload, headers=headers, timeout=5)
        print(f"✅ [INFO] POST {ALARM_API} -> {resp.status_code} {resp.reason}")
        print("[DEBUG] req payload:", payload)
        print("[DEBUG] resp body:", resp.text)

    except Exception as e:
        print(f"[ERROR] send alert failed: {e}")
        db.rollback()
    finally:
        db.close()
