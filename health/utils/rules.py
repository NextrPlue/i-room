# health/utils/rules.py
# 목적:
#   - 모델의 출력(이상, 정상)을 "맥락 룰"로 후 처리하여 오탐(FP)을 줄인다.
#   - 룰은 가볍고 실시간 친화적으로 설계(연산량, 해석가능성 고려)
#
# 반환:
#   - apply_rules(...) -> (final_pred, reason)
#   - final_pred: 0(정상), 1(이상)
#   - reason    : 해당 결론에 이른 사유 문자열(로깅)
#
# 기본 아이디어:
#   - R1(저심박 억제): 모델이 "이상"이라도 심박이 "정상/낮음"이면 억제 -> 이상 아님
#   - R2(활동 중 정상화): 활동강도가 충분하고 그에 비해 심박이 과하지 않으면 억제
#   - R3(저활동 고심박 확인): 활동 거의 없는데 심박만 높으면 모델 판단 유지, 승격 -> 실제 위험 가능성
#   - 회색대(gray), 강확신(strong) 구간: 모델 확신도에 따라 룰 적용 강도 조절
#   - Sanity check: HR, 입력값 이상치 필터링

from dataclasses import dataclass

@dataclass
class RuleConfig:
    # R1: 저심박 억제(Veto) 관련 파라미터
    hr_ratio_veto: float = 0.60 # HR/HRmax가 이 값보다 낮으면 "저심박"
    hr_normal_cap: int = 100    # HR가 이 값 미만이면 "정상 범위"라고 더 강하게 판단

    # R2: 활동 중 정상화(Suppress) 관련 파라미터
    intensity_on: int = 60              # 활동 점수(0~100)가 이 이상이면 "활동 중"으로 판단
    hr_ratio_active_max: float = 0.85   # 활동 중인 경우, HR/HRmax가 이 값 이하라면 "활동 대비 과도하지 않다"고 보고 억제

    # R3: 저활동 고심박 Confirm 관련 파라미터
    low_steps_threshold: int = 20   # 분당 걸음(steps per minute)이 이 이하이면 "저활동"
    hr_ratio_high: float = 0.90     # HR/HRmax가 이 이상이면 "고심박"

    # 모델 확신도(회색대/강확신) 경계
    gray_delta: float = 0.10    # threshold ~ threshold+0.10: 회색대(불확실)
    hard_delta: float = 0.20    # threshold+0.20 이상: 강한 확신(룰 간섭 최소화)

# 기본 설정값 인스턴스
CFG = RuleConfig()

def _hrmax(age: float) -> float:
    """
    나이 기반 최대심박수(HRmax) 간단 추정치.
        - 공식: HRmax = 220 - Age
        - 0 나눗셈 방지를 위해 최소 1 보장
    """
    return max(220.0 - float(age), 1.0)

def _intensity_score(spm: float | None, speed_mps: float | None, pace_min_per_km: float | None) -> float:
    """
    활동 강도 점수(0~100)를 간단 계산.
        - 가능한 입력: steps_per_minute(spm), 속도(m/s), 페이스(min/km)
        - 세 입력 중 "가장 높은 강도"를 대표값으로 사용
        - 정규화 기준은 보수적으로 설정(ex: 3.5m/s는 12.6km/h -> 100점)
    """
    # steps per minute 기반 점수: 180 spm -> 100점으로 정규화 (러닝 캐던스 상한을 보수적으로)
    s1 = (spm / 180.0 * 100.0) if spm is not None else 0.0

    # speed 기반 점수: 3.5m/s를 100점으로 매핑 (빠른 러닝 수준)
    s2 = (speed_mps / 3.5 * 100.0) if speed_mps is not None else 0.0

    # pace(min/km) -> speed(m/s) 변환 후 점수화
    s3 = 0.0
    if pace_min_per_km is not None and pace_min_per_km > 0:
        # pace(min/km) -> 속도(m/s) = 1000(m) / (min*60)
        speed = 1000.0 / (pace_min_per_km * 60.0)
        s3 = (speed / 3.5 * 100.0)

    # 세 값 중 최대를 대표 강도로 사용. 0~100으로 클리핑
    return max(0.0, min(100.0, max(s1, s2, s3)))

def apply_rules(age, hr, steps_per_minute=None, speed_mps=None, pace_min_per_km=None,
                model_pred=0, model_proba=0.0, threshold=0.5, cfg: RuleConfig = CFG):
    """
    모델 결과(model_pred/proba/threshold)와 맥락(나이, HR, 활동량)을 종합해
    최종 판단(0/1)과 그 사유(reason 문자열)를 반환.

    파라미터:
        - age: 나이 -> HRmax 계산에 사용
        - hr : 현재 심박수(BPM)
        - steps_per_minute: 분당 걸음 수(없으면 None)
        - speed_mps: 현재 속도(m/s, 없으면 None)
        - model_pred: 모델의 이진 예측(0/1)
        - model_proba: 모델의 위험(1) 확률(0~1)
        - threshold: 분류 임계값(검증에서 선택한 값)
        - cfg: RuleConfig. 운영에서 수치 미세조정 시 주입 가능

    반환:
        - (final_pred, reason)
        - final_pred: 0=정상, 1=이상
        - reason    : "왜 그렇게 판단했는지" 한 줄 사유(로깅)
    """

    # 파생값 계산
    hrmax = _hrmax(age) # 나이 기반 최대 심박
    hr_ratio = float(hr) / hrmax if hr is not None else 0.0 # HR/HRmax -> 개인화된 상대강도
    intensity = _intensity_score(steps_per_minute, speed_mps, pace_min_per_km)  # 활동 강도 점수(0~100)

    # Sanity 체크(갤럭시워치 값 이상/결측 확인)
    # HR가 비정상 범위면 모델/룰 적용 대신 "무시/정상" 처리를 선택
    if hr is None or hr < 30 or hr > 220:
        return 0, "invalid_hr"  # HR 센서 오류 가능성 -> 경보 억제

    # 모델 확신도 구간 분류
    #   - 회색대(gray): threshold ~ threshold + gray_delta
    #       -> 확신이 낮음. 억제 룰(R1/R2)을 적극 적용.
    #   - 강확신(strong): threshold + hard_delta 이상
    #       -> 확신이 높음. 억제 룰 간섭을 최소화(모델 판단을 더 존중).
    gray = (threshold <= model_proba < threshold + cfg.gray_delta)
    strong = (model_proba >= threshold + cfg.hard_delta)

    # 모델이 정상이라고 이미 말했으면 그대로 정상
    if model_pred == 0:
        return 0, "model_normal"

    # R1: 저심박 억제(Veto)
    #   - "운동이 아닌데" 모델이 이상이라면 FP일 확률이 높다.
    #   - 다음 두 조건을 동시에 만족하면 "정상으로 덮어쓰기"
    #       -> (1) 상대심박(hr_ratio) 낮음 AND (2) HR 100 미만
    #   - 단, strong(강확신) 구간에서는 룰 간섭을 줄인다.
    if not strong:
        if (hr_ratio < cfg.hr_ratio_veto) and (hr < cfg.hr_normal_cap):
            return 0, "rule_suppress_low_hr"

    # R2: 활동 중 정상화
    #   - "충분히 활동 중"이고 "활동 대비 심박이 과도하지 않으면" 억제.
    #   - intensity >= 60(활동 중) AND hr_ratio <= 0.85(활동 대비 과도X)
    #   - 단, string(강확신) 구간에서는 룰 간섭을 줄인다.
    if intensity >= cfg.intensity_on and hr_ratio <= cfg.hr_ratio_active_max and not strong:
        return 0, "rule_suppress_active"

    # R3: 저활동 고심박 Confirm
    #   - "거의 움직이지 않는데" 심박만 높은 경우는 실제 위험 가능성이 있음.
    #   - steps_per_minute <= 20(저활동) AND hr_ratio >= 0.90(고심박)
    #   - 이 경우는 모델 판단 유지, 승격
    if (steps_per_minute is None or steps_per_minute <= cfg.low_steps_threshold) and hr_ratio >= cfg.hr_ratio_high:
        return 1, "rule_confirm_high_hr_low_activity"

    # 기본: 모델 판단 유지
    return 1, "model_confirm"