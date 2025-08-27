# ppe/services/ppe_check.py
from typing import List, Dict, Any, Tuple
from sqlalchemy.orm import Session
from ppe.orm.watch import Watch
from ppe.database import SessionLocal

# 상태유지, 객체 일관성, OFF 오탐 억제
from time import time
import threading
import math

# 이미 미착용 판별한 고유 track id의 보호구를 다시 미착용 알람을 보내지 않도록 하는 변수
# 고유 track id 별로 incident ACTIVE를 설정,
COOLDOWN_S = 120       # 같은 incident ACTIVE 중 "지속 요약" 최소 간격
EXPIRE_S   = 30        # 트랙 끊김 자동 해제 TTL(초) 

# track id가 달라지더라도 같은 id임을 판별하는 경우, -> MERGE_T 동안 유사 거리에 있는 BBOX면 같은 ID라고 판별
MERGE_T    = 5.0       # tid 스위치 병합 허용 시간차(초) 
BASE_R     = 80.0      # 병합 허용 거리(px). bbox 높이에 비례 스케일

# OFF_HOLD_S의 값(초) 동안 미착용 OFF의 히트 수 OFF_HOLD_MIN_HIT의 수만큼 되면 미착용 판별
OFF_HOLD_S       = 5.0   # 같은 tid의 OFF가 이 시간 안에 반복되면 유효
OFF_HOLD_MIN_HIT = 20     # 최소 OFF 히트 수

# [추가] ON-근접 억제(사전 억제): 방금 강한 ON이면 OFF 억제
ON_GRACE_S       = 3.0   # 최근 강한 ON을 본 뒤 이 시간 내 OFF는 억제 후보
STRONG_ON_CONF   = 0.65  # "강한 ON"으로 간주할 conf
OFF_MARGIN       = 0.1  # 억제 해제 마진: off_conf가 on_conf + margin 이상이면 통과

_active_lock = threading.Lock()
# key = (ppe_type, tid)  e.g. ('helmet_off', 42)
_active_off: Dict[Tuple[str, int], Dict[str, Any]] = {}

# OFF-홀드 임시 누적: key=(ppe_type, tid) -> {first_t,last_t,hits,max_conf}
_off_hold: Dict[Tuple[str,int], Dict[str,Any]] = {}

# 최근 강한 ON 기록: key=(ppe_base, tid) -> {t, conf}
# 'helmet' | 'harness'  (name에서 공통으로 추출)
_recent_strong_on: Dict[Tuple[str,int], Dict[str,Any]] = {}
ON_KEEP_S = 10.0  # 강한 ON 기록 유지 시간(메모리 정리용)

# 기본 임계값
BASE_CONF = {
    "harness_off": 0.73,
    "helmet_off":  0.6,
}
# 너무 작은 바운딩박스는 OFF 확정 금지
MIN_BBOX = 50

def read_total_workers() -> int:
    db: Session = SessionLocal()
    try:
        return db.query(Watch.worker_id).distinct().count()
    finally:
        db.close()

def bbox_height(xyxy: Tuple[float,float,float,float]) -> float:
    x1,y1,x2,y2 = xyxy
    return max(0.0, y2 - y1)

# 상태, OFF 오탐 방지
# 이벤트 name에서 ppe_base('helmet'|'harness') 추출
def _ppe_base(name: str) -> str:
    if "helmet" in name: return "helmet"
    if "harness" in name: return "harness"
    return ""

# 오래 안 보인 ACTIVE incident 자동 해제 + 홀드/ON 기록 청소
def _gc(now_t: float) -> None:
    with _active_lock:
        # incident 만료
        dead = [k for k, v in _active_off.items() if now_t - v["last_seen"] > EXPIRE_S]
        for k in dead:
            _active_off.pop(k, None)
        # 홀드 만료
        hold_dead = [k for k, v in _off_hold.items() if now_t - v["last_t"] > OFF_HOLD_S]
        for k in hold_dead:
            _off_hold.pop(k, None)
        # 최근 강한 ON 만료
        on_dead = [k for k, v in _recent_strong_on.items() if now_t - v["t"] > ON_KEEP_S]
        for k in on_dead:
            _recent_strong_on.pop(k, None)

# 신뢰도가 높은 ON 기록이 찍힌 기록이 있으면 OFF 로그가 찍혀도 무시 가능(억제)
def _note_strong_on(nm: str, tid: int, t: float, conf: float) -> None:
    if tid == -1: return
    if conf < STRONG_ON_CONF: return
    base = _ppe_base(nm)
    if not base: return
    with _active_lock:
        _recent_strong_on[(base, tid)] = {"t": t, "conf": conf}

# OFF-홀드 누적/판정 return True  -> 홀드 조건 충족(알람 허용), return False -> 아직 홀드 중(알람 억제)
def _off_hold_update(ppe_type: str, tid: int, t: float, conf: float) -> bool:
    key = (ppe_type, tid)
    with _active_lock:
        rec = _off_hold.get(key)
        if rec and (t - rec["first_t"]) <= OFF_HOLD_S:
            rec["last_t"]  = t
            rec["hits"]   += 1
            rec["max_conf"]= max(rec["max_conf"], conf)
        else:
            _off_hold[key] = {"first_t": t, "last_t": t, "hits": 1, "max_conf": conf}

        ok = _off_hold[key]["hits"] >= OFF_HOLD_MIN_HIT
        if ok:
            # 홀드 통과했으니 버퍼 제거(중복 생성 방지)
            _off_hold.pop(key, None)
        return ok

# def _resolve_on(ppe_type: str, tid: int, t: float) -> None:
#     return

# OFF 관측을 incident에 반영. 없으면 새 incident 생성 -> 이때만 알림이 발생함 
def _upsert_off(
    ppe_type: str,
    tid: int,
    t: float,
    conf: float,
    uv: Tuple[float, float],
    bbox_h: float,
) -> Tuple[bool, Dict[str, Any]]:
    with _active_lock:
        key = (ppe_type, tid)
        rec = _active_off.get(key)
        if rec is None:
            incident_id = f"{ppe_type}-{tid}-{int(t)}"
            rec = {
                "incident_id": incident_id,
                "ppe_type": ppe_type,
                "tid": tid,
                "first_seen": t,
                "last_seen": t,
                "last_sent": 0.0,
                "max_conf": conf,
                "last_uv": uv,
                "last_bbox_h": bbox_h,
                "state": "ACTIVE",
            }
            _active_off[key] = rec
            return True, rec
        else:
            rec["last_seen"]   = max(rec["last_seen"], t)
            rec["max_conf"]    = max(rec["max_conf"], conf)
            rec["last_uv"]     = uv
            rec["last_bbox_h"] = bbox_h
            return False, rec

# tid 바뀌어도 같은 사람으로 간주되면 기존 incident로 병합(알람 없음)
def _try_merge_off(ppe_type: str, ev: Dict[str, Any]) -> Tuple[Dict[str, Any], bool]:

    t   = float(ev.get("t", 0.0))
    uv  = ev.get("uvb", None)
    if not uv:
        return None, False
    xyxy = ev.get("xyxy")
    hb   = bbox_height(xyxy) if xyxy else 50.0

    with _active_lock:
        best = (1e9, None)
        for (typ, _tid), rec in _active_off.items():
            if typ != ppe_type:
                continue
            if t - rec["last_seen"] > MERGE_T:
                continue
            du = uv[0] - rec["last_uv"][0]
            dv = uv[1] - rec["last_uv"][1]
            dist = math.hypot(du, dv)
            h = max(1.0, rec.get("last_bbox_h", 50.0))
            thr = BASE_R * (h / 100.0)
            if dist <= thr and dist < best[0]:
                best = (dist, rec)

        if best[1] is None:
            return None, False

        rec = best[1]
        new_tid = ev.get("tid", rec["tid"])
        if new_tid != rec["tid"]:
            old_key = (ppe_type, rec["tid"])
            new_key = (ppe_type, new_tid)
            rec["tid"] = new_tid
            _active_off.pop(old_key, None)
            _active_off[new_key] = rec

        rec["last_seen"]   = t
        rec["last_uv"]     = uv
        rec["last_bbox_h"] = hb
        rec["max_conf"]    = max(rec["max_conf"], float(ev.get("conf", 0.0)))
        return rec, True
    

# 커버리지 기반 동적 임계
def dynamic_thrs(
    on_events: List[Dict[str, Any]],
    off_events: List[Dict[str, Any]],
    total_workers: int,
):
    tids_helmet  = set()
    tids_harness = set()
    for e in on_events:
        nm, tid = e.get("name"), e.get("tid", -1)
        if tid == -1: continue
        if nm in ("helmet_on", "helmet_off"):   tids_helmet.add(tid)
        if nm in ("harness_on","harness_off"):  tids_harness.add(tid)
    for e in off_events:
        nm, tid = e.get("name"), e.get("tid", -1)
        if tid == -1: continue
        if nm in ("helmet_on", "helmet_off"):   tids_helmet.add(tid)
        if nm in ("harness_on","harness_off"):  tids_harness.add(tid)

    observed_pairs = len(tids_helmet) + len(tids_harness)
    expected_pairs = max(1, total_workers * 2)
    ratio = min(1.0, observed_pairs / expected_pairs) if total_workers > 0 else 0.0

    if   ratio >= 0.95: trust, delta = "High",    -0.05
    elif ratio >= 0.75: trust, delta = "Normal",   0.00
    elif ratio >= 0.50: trust, delta = "Low",     +0.05
    else:               trust, delta = "VeryLow", +0.10

    dyn_thr = {
        "harness_off": min(0.99, max(0.0, BASE_CONF["harness_off"] + delta)),
        "helmet_off":  min(0.99, max(0.0, BASE_CONF["helmet_off"]  + delta)),
    }
    return {
        "observed_pairs": observed_pairs,
        "expected_pairs": expected_pairs if total_workers > 0 else 0,
        "coverage_ratio": round(ratio, 3),
        "trust": trust,
        "dynamic_off_thr": dyn_thr,
    }

# on/off 이벤트 리스트를 받아 10초 윈도우 위반 판단 수행.
def check_violation(
    on_events: List[Dict[str, Any]],
    off_events: List[Dict[str, Any]],
) -> Dict[str, Any]:
    
    # (1) 통계 집계(표시용)
    helm_on_tids   = {e["tid"] for e in on_events  if e.get("name")=="helmet_on"  and e.get("tid",-1)!=-1}
    helm_off_tids  = {e["tid"] for e in off_events if e.get("name")=="helmet_off" and e.get("tid",-1)!=-1}
    har_on_tids    = {e["tid"] for e in on_events  if e.get("name")=="harness_on" and e.get("tid",-1)!=-1}
    har_off_tids   = {e["tid"] for e in off_events if e.get("name")=="harness_off"and e.get("tid",-1)!=-1}

    total_workers = read_total_workers()

    # (2) 동적 임계 계산
    cov = dynamic_thrs(on_events, off_events, total_workers)
    dyn_thr = cov["dynamic_off_thr"]

    # (3) 최근 강한 ON 기록(사전 억제용)
    now_t = 0.0
    for ev in (on_events or []):
        t = float(ev.get("t", 0.0))
        if t > now_t: now_t = t
        nm = ev.get("name", "")
        tid = ev.get("tid", -1)
        conf = float(ev.get("conf", 0.0))
        _note_strong_on(nm, tid, t, conf)

    # (4) OFF-Strict 선별
    accepted_off_events: List[Dict[str, Any]] = []
    accepted_tids = {"helmet_off": set(), "harness_off": set()}

    for ev in off_events:
        name = ev.get("name", "")
        if name not in ("harness_off", "helmet_off"):
            continue
        conf = float(ev.get("conf", 0.0))
        xyxy = ev.get("xyxy", None)
        if xyxy is None:
            continue

        min_conf = dyn_thr.get(name, BASE_CONF.get(name, 0.55))
        if conf < min_conf:
            continue
        if bbox_height(xyxy) < MIN_BBOX:
            continue

        tid = ev.get("tid", -1)
        if tid != -1:
            accepted_tids[name].add(tid)
            accepted_off_events.append(ev)

        t = float(ev.get("t", 0.0))
        if t > now_t: now_t = t

    if now_t == 0.0:
        now_t = time()

    # (5) 만료 청소
    _gc(now_t)

    # (6) 상태머신 반영 & 알림 결정
    new_alerts = []

    for ev in accepted_off_events:
        name = ev.get("name")
        base = _ppe_base(name)                 # 'harness' | 'helmet'
        tid  = ev.get("tid", -1)
        if tid == -1:
            continue

        t    = float(ev.get("t", now_t))
        conf = float(ev.get("conf", 0.0))
        xyxy = ev.get("xyxy")
        uv   = ev.get("uvb", None) or (0.0, 0.0)
        hb   = bbox_height(xyxy) if xyxy else 50.0

        # 6-1) 기존 incident로 병합 가능하면(=이미 같은 사람 ACTIVE) 알람 불필요
        _, merged = _try_merge_off(name, ev)
        if merged:
            continue

        # 6-2) [추가] ON-근접 억제: 최근 강한 ON이 있으면 OFF 억제(사전 차단)
        #  - 같은 tid, 같은 ppe_base에 대해 최근 ON이 grace 창 안이고
        #  - off_conf 가 on_conf + margin 미만이면 억제
        on_key = (base, tid)
        on_rec = _recent_strong_on.get(on_key)
        if on_rec and (t - on_rec["t"]) <= ON_GRACE_S:
            if conf < (on_rec["conf"] + OFF_MARGIN):
                # 최근 강한 ON 이후의 순간 오탐으로 간주 → 알람 억제
                continue

        # 6-3) [추가] OFF-홀드(디바운스) 충족 전에는 알람 억제
        if not _off_hold_update(name, tid, t, conf):
            # 아직 홀드 중(히트 수 부족 또는 시간 창 벗어남) → 알람 억제
            continue

        # 6-4) 홀드 통과 → 비로소 incident 생성/갱신 (여기서만 새 알람 가능)
        is_new, rec = _upsert_off(name, tid, t, conf, uv, hb)
        if is_new:
            rec["last_sent"] = t
            new_alerts.append({
                "incident_id": rec["incident_id"],
                "ppe_type": name,
                "tid": tid,
                "first_seen": rec["first_seen"],
                "conf": rec["max_conf"],
            })
        else:
            if (t - rec.get("last_sent", 0.0)) >= COOLDOWN_S:
                # 필요 시 “지속 중” 요약 알림 사용(기본 끔)
                # rec["last_sent"] = t
                # new_alerts.append({...})
                pass

    off_helmet_distinct  = len(accepted_tids["helmet_off"])
    off_harness_distinct = len(accepted_tids["harness_off"])

    if new_alerts:
        reason = (
            f'미착용자 발생'
            f'(helmet_off {off_helmet_distinct}건, harness_off {off_harness_distinct}건, '
            f'등록된 총 근로자 수={total_workers},'
        )
        return {"violation": True, "reason": reason, "new_alerts": new_alerts, "coverage": cov}

    reason = (
        '이번 윈도우에서 새롭게 확정된 미착용자가 없습니다. '
    )
    return {"violation": False, "reason": reason, "new_alerts": [], "coverage": cov}
