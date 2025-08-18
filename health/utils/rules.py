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

    suppress_active_when_strong: bool = True   # 강확신에서도 억제 허용할지
    intensity_on_strong: int = 80              # 강확신이면 활동 기준을 더 높임
    hr_ratio_active_max_strong: float = 0.80   # 강확신이면 상대심박 기준을 더 빡빡하게

    proba_very_strong: float = 0.90            # 이 이상 확률이면 절대 억제하지 않음
    hr_ratio_very_high: float = 0.93           # 이 이상 상대심박이면 절대 억제하지 않음

    # R3: 저활동 고심박 Confirm 관련 파라미터
    low_steps_threshold: int = 20   # 분당 걸음(steps per minute)이 이 이하이면 "저활동"
    hr_ratio_high: float = 0.90     # HR/HRmax가 이 이상이면 "고심박"

    # 모델 확신도(회색대/강확신) 경계
    gray_delta: float = 0.10    # threshold ~ threshold+0.10: 회색대(불확실)
    hard_delta: float = 0.20    # threshold+0.20 이상: 강한 확신(룰 간섭 최소화)
    strong_abs_min: float = 0.80    # proba가 이 수치보다 낮으면 강한 확신 아님

# 기본 설정값 인스턴스
CFG = RuleConfig()

def _hrmax(age: float) -> float:
    """
    나이 기반 최대심박수(HRmax) 간단 추정치.
        - 공식: HRmax = 220 - Age
        - 0 나눗셈 방지를 위해 최소 1 보장
    """
    return max(220.0 - float(age), 1.0)

def _intensity_score(spm: float | None, speed_kmh: float | None, pace_min_per_km: float | None) -> float:
    """
    활동 강도 점수(0~100)를 간단 계산.
        - 입력: 
            spm: steps_per_minute (분당 걸음 수)
            speed_kmh: 속도 km/h (갤럭시 워치 기준에 맞춤)
            pace_min_per_km: 페이스 (분/킬로미터)
        - 정규화:
            * SPM 180 -> 100점
            * 속도 12.6 km/h(=3.5 m/s) -> 100점
            * 페이스는 속도로 변환 후 동일 기준 적용
        - 세 입력 중 "가장 높은 강도"를 대표값으로 사용
    """
    # steps per minute 기반 점수: 180 spm -> 100점으로 정규화 (러닝 캐던스 상한을 보수적으로)
    s1 = (spm / 180.0 * 100.0) if spm is not None else 0.0

    # speed 기반 점수: 12.6 km/h를 100점으로 매핑 (빠른 러닝 수준)
    s2 = (speed_kmh / 12.6 * 100.0) if (speed_kmh is not None) else 0.0

    # pace(min/km) -> speed(m/s) -> km/h 변환 후 점수화
    s3 = 0.0
    if pace_min_per_km is not None and pace_min_per_km > 0:
        # pace(min/km) -> 속도(m/s) = 1000(m) / (min*60)
        speed_mps = 1000.0 / (pace_min_per_km * 60.0)   # m/s
        speed_kmh_from_pace = speed_mps * 3.6
        s3 = (speed_kmh_from_pace / 12.6 * 100.0)

    # 세 값 중 최대를 대표 강도로 사용. 0~100으로 클리핑
    return max(0.0, min(100.0, max(s1, s2, s3)))

def apply_rules(age, hr, steps_per_minute=None, speed_kmh=None, pace_min_per_km=None,
                model_pred=0, model_proba=0.0, threshold=0.5, cfg: RuleConfig = CFG):
    """
    모델 결과(model_pred/proba/threshold)와 맥락(나이, HR, 활동량)을 종합해
    최종 판단(0/1)과 그 사유(reason 문자열)를 반환.

    파라미터:
        - age: 나이 -> HRmax 계산에 사용
        - hr : 현재 심박수(BPM)
        - steps_per_minute: 분당 걸음 수(없으면 None)
        - speed_kmh: 현재 속도(km/h, 없으면 None)
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
    intensity = _intensity_score(steps_per_minute, speed_kmh, pace_min_per_km)  # 활동 강도 점수(0~100)

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
    cut = threshold + cfg.hard_delta             # 상대 기준 (t + 0.20)
    strong_cut = max(cut, cfg.strong_abs_min)    # 절대 하한(0.80)
    strong = (model_proba >= strong_cut)

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

    # R2: 활동 중 정상화 (strong, very_strong까지 포괄 적용)
    # 모델의 위험 예측 확률이 0.90이상 또는 hr_ratio가 0.93이상 -> 매우 강한 확신
    very_strong = (model_proba >= cfg.proba_very_strong) or (hr_ratio >= cfg.hr_ratio_very_high)

    # strong 여부에 따라 기준 가변
    # strong(강확신)이고, 강확신에서 룰 적용이 허용되어 있으면,
    # -> 활동 점수 기준 80
    # -> 상대 심박 기준 0.80
    eff_intensity_on = cfg.intensity_on_strong if (strong and cfg.suppress_active_when_strong) else cfg.intensity_on
    eff_hr_cut = cfg.hr_ratio_active_max_strong if (strong and cfg.suppress_active_when_strong) else cfg.hr_ratio_active_max

    # very_strong(매우 강한 확신): 활동 점수 기준 = 80, 상대심박 = 0.93
    # 너무 강한 신호는 절대 억제하지 않음
    if (not very_strong) and (intensity >= eff_intensity_on) and (hr_ratio <= eff_hr_cut):
        return 0, "rule_suppress_active" + ("_strong" if strong else "")

    # R3: 저활동 고심박 Confirm
    #   - "거의 움직이지 않는데" 심박만 높은 경우는 실제 위험 가능성이 있음.
    #   - steps_per_minute <= 20(저활동) AND hr_ratio >= 0.90(고심박)
    #   - 이 경우는 모델 판단 유지, 승격
    if (steps_per_minute is None or steps_per_minute <= cfg.low_steps_threshold) and hr_ratio >= cfg.hr_ratio_high:
        return 1, "rule_confirm_high_hr_low_activity"

    # 기본: 모델 판단 유지
    return 1, "model_confirm"