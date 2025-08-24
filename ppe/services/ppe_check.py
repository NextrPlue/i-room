# ppe/services/ppe_check.py
from typing import List, Dict, Any, Tuple
from sqlalchemy.orm import Session
from ppe.orm.watch import Watch
from ppe.database import SessionLocal

# 기본 임계값
BASE_CONF = {
    "harness_off": 0.7, # 안전벨트 미착용의 신뢰도가 0.7 이상이면 알람 발생
    "helmet_off":  0.6, # 안전모 미착용의 신뢰도가 0.6 이상이면 알람 발생
}
 # 바운딩박스의 길이(px)가 이 값보다 작으면 OFF 확정 하지 않는 가시성 필터적용 
MIN_BBOX = 50

def read_total_workers() -> int:
    db: Session = SessionLocal()
    try:
        return db.query(Watch.worker_id).distinct().count() # 근로자 (worker_id) 수 가져오기
    finally:
        db.close()

def bbox_height(xyxy: Tuple[float,float,float,float]) -> float:
    x1,y1,x2,y2 = xyxy
    return max(0.0, y2 - y1)

# 커버리지계산 → 신뢰도와 동적 임계 만들기
def dynamic_thrs(
    on_events: List[Dict[str, Any]],
    off_events: List[Dict[str, Any]],
    total_workers: int,
):
    # PPE 타입별로 한 번이라도 관측된 track_id 집합 -> Watch_workerId의 총갯수와 비교하기 위함
    tids_helmet  = set() # 한번이라도 관측된 헬멧
    tids_harness = set() # 한버이라도 관측된 안전벨트
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

    observed_pairs = len(tids_helmet) + len(tids_harness) # 탐지된 객체의 갯수
    expected_pairs = max(1, total_workers * 2)  # 0 방지,  Watch_workerId의 총갯수 * 2
    ratio = min(1.0, observed_pairs / expected_pairs) if total_workers > 0 else 0.0

    # 신뢰도 레벨 및 보정치
    if   ratio >= 0.95: trust, delta = "High",     -0.05
    elif ratio >= 0.75: trust, delta = "Normal",    0.00
    elif ratio >= 0.50: trust, delta = "Low",      +0.05
    else:               trust, delta = "VeryLow",  +0.10

    # 기본 OFF 임계에 delta를 더해 동적 임계로 로직 발생
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

def check_violation(
    on_events: List[Dict[str, Any]],
    off_events: List[Dict[str, Any]],
) -> Dict[str, Any]:
    """
    on/off 이벤트 리스트를 받아 10초 윈도우 위반 판단 수행.
    이벤트 예:
      {'t': float, 'name': 'harness_on|harness_off|helmet_on|helmet_off',
       'conf': float, 'xyxy': (x1,y1,x2,y2), 'uvb': (u,v), 'tid': int}
    """
    # [수정] distinct(고유 track_id) 기준 집계
    helm_on_tids   = {e["tid"] for e in on_events  if e.get("name")=="helmet_on"  and e.get("tid",-1)!=-1}   # helmet_on의 고유 tid 수
    helm_off_tids  = {e["tid"] for e in off_events if e.get("name")=="helmet_off" and e.get("tid",-1)!=-1}   # helmet_off의 고유 tid 수
    har_on_tids    = {e["tid"] for e in on_events  if e.get("name")=="harness_on" and e.get("tid",-1)!=-1}   # harness_on의 고유 tid 수
    har_off_tids   = {e["tid"] for e in off_events if e.get("name")=="harness_off"and e.get("tid",-1)!=-1}   # harness_off의 고유 tid 수

    total_workers = read_total_workers()

    # 1) 커버리지 기반 동적 임계 계산
    cov = dynamic_thrs(on_events, off_events, total_workers)
    dyn_thr = cov["dynamic_off_thr"]

    # 2) OFF-Strict: (동적 임계) + 가시성  → distinct(고유 tid)로만 카운트
    accepted_tids = {"helmet_off": set(), "harness_off": set()}  # [수정] 트랙당 최대 1건
    for ev in off_events:
        name = ev.get("name", "")  # 기본필터 1 -> harness_off 또는 helmet_off 이냐? 검사
        if name not in ("harness_off", "helmet_off"):
            continue
        conf = float(ev.get("conf", 0.0))
        xyxy = ev.get("xyxy", None)
        if xyxy is None: # 기본필터 2 -> bbox의 유무 검사
            continue

        min_conf = dyn_thr.get(name, BASE_CONF.get(name, 0.55)) # 기본필터 3 -> 동적임계 검사
        if conf < min_conf:
            continue
        if bbox_height(xyxy) < MIN_BBOX: # 기본필터 4 -> 가시성 게이트(너무 작은 건 OFF 확정 금지)
            continue

        tid = ev.get("tid", -1)
        if tid != -1:
            accepted_tids[name].add(tid)

    off_helmet_distinct  = len(accepted_tids["helmet_off"])
    off_harness_distinct = len(accepted_tids["harness_off"])

    if (off_helmet_distinct + off_harness_distinct) > 0: # OFF-Strict 위반 반환
        reason = (
            f'미착용자 발생 근거는 "OFF-Strict 통과(helmet_off {off_helmet_distinct}건, harness_off {off_harness_distinct}건)" 입니다.\n'
            f'GPS 등록된 총 근로자 수={total_workers}, 탐지 수(클래스별)='
            f'[helmet_on:{len(helm_on_tids)}, helmet_off:{len(helm_off_tids)}, '
            f'harness_on:{len(har_on_tids)}, harness_off:{len(har_off_tids)}].'
        )
        return {"violation": True, "reason": reason}

    # # 3) (보조 검사) 총원 vs 착용 갭(min값 기준) 비교
    # detected_with_ppe = min(len(har_on_tids), len(helm_on_tids))  # [수정] distinct 기준
    # gap = max(0, total_workers - detected_with_ppe)

    # if total_workers > 0 and gap > 0:
    #     reason = (
    #         f'미착용자 발생 근거는 "총원 대비 착용 인원 부족(gap={gap})" 입니다.\n'
    #         f'GPS 등록된 총 근로자 수={total_workers}, 탐지 수(클래스별)='
    #         f'[helmet_on:{len(helm_on_tids)}, helmet_off:{len(helm_off_tids)}, '
    #         f'harness_on:{len(har_on_tids)}, harness_off:{len(har_off_tids)}].'
    #     )
    #     return {"violation": True, "reason": reason}

    # # 4) 위반 없음
    # reason = (
    #     f'미착용자 발생 근거가 확인되지 않았습니다.\n'
    #     f'GPS 등록된 총 근로자 수={total_workers}, 탐지 수(클래스별)='
    #     f'[helmet_on:{len(helm_on_tids)}, helmet_off:{len(helm_off_tids)}, '
    #     f'harness_on:{len(har_on_tids)}, harness_off:{len(har_off_tids)}].'
    # )
    # return {"violation": False, "reason": reason}
