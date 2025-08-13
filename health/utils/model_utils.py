# health/utils/model_utils.py

import pickle
import numpy as np
import pandas as pd
import os
import json

# 기본 경로 설정 (프로젝트 루트 기준)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# 모델, 메타 경로
# XGBoost 모델(JSON) + 메타데이터(JSON) 사용
XGB_MODEL_PATH = os.path.join(BASE_DIR, "models", "baseline_xgb.json")
META_PATH = os.path.join(BASE_DIR, "models", "baseline_meta.json")

# 기존 LightGBM 모델(.pkl)이 있을 경우 폴백
LGB_PICKLE_PATH = os.path.join(BASE_DIR, "models", "lgb_model_2.pkl")

# 전역 객체 (lazy-load)
_booster = None     # XGBoost Booster
_lgb_model = None   # LightGBM 모델 (scikit API)
_meta = None        # meta.json (threshold, feature_cols, preprocessing, best_iteration, calibration)

# 모델 로딩 (우선 XGB, 없으면 LGBM)
def _load_artifacts():
    global _booster, _lgb_model, _meta

    if _meta is None and os.path.exists(META_PATH):
        with open(META_PATH, "r") as f:
            _meta = json.load(f)

    # XGBoost 우선
    if _booster is None and os.path.exists(XGB_MODEL_PATH):
        import xgboost as xgb   # 지연 import
        _booster = xgb.Booster()
        _booster.load_model(XGB_MODEL_PATH)
        return
    
    # 없으면 LightGBM 폴백
    if _lgb_model is None and os.path.exists(LGB_PICKLE_PATH):
        with open(LGB_PICKLE_PATH, "rb") as f:
            _lgb_model = pickle.load(f)

# 전처리 함수: 입력 -> 피처 DataFrame으로 변환
def _preprocess_to_features(age, heart_rate, meta: dict | None) -> pd.DataFrame:
    """훈련 당시와 동일한 전처리/파생/피처순서로 입력을 정렬"""
    # 1) 필수 입력 유효성 검사
    if age is None or heart_rate is None:
        raise ValueError("age와 heart_rate는 필수입니다.")
    
    age = float(age)
    hr = float(heart_rate)

    # 2) 결측 대체(훈련 통계 사용)
    # Age 결측의 경우 훈련시 median으로 채웠으므로 meta에 저장된 값을 사용
    if np.isnan(age):
        if meta and "preprocessing" in meta and "age_fillna_median" in meta["preprocessing"]:
            age = float(meta["preprocessing"]["age_fillna_median"])
        else:
            # 기본 값 사용
            age = 40.0

    # HR 결측은 드롭이 원칙 -> None 반환 또는 예외 처리
    if np.isnan(hr):
        raise ValueError("heart_rate(HR)가 결측입니다.")

    # 3) 파생 피처
    hrmax = max(220.0 - age, 1.0)   # Divide by Zero 방지
    hr_ratio = hr / hrmax

    # 4) 피처 구성, 순서 고정
    # meta에 feature_cols가 있으면 그 순서대로 사용, 없으면 기본 순서 사용
    default_cols = ["Age", "HR", "HRmax", "hr_ratio"]
    cols = meta.get("feature_cols", default_cols) if meta else default_cols
    feat = {
        "Age": age,
        "HR": hr,
        "HRmax": hrmax,
        "hr_ratio": hr_ratio
    }

    X = pd.DataFrame([[feat[c] for c in cols]], columns=cols)

    return X

# XGBoost 예측 함수
def _predict_proba_xgb(X: pd.DataFrame, booster, meta: dict | None) -> np.ndarray:
    import xgboost as xgb
    dmat = xgb.DMatrix(X)
    it_end = None
    if meta and "best_iteration" in meta and meta["best_iteration"] is not None:
        it_end = int(meta["best_iteration"])
    if it_end and it_end > 0:
        p = booster.predict(dmat, iteration_range=(0, it_end))
    else:
        p = booster.predict(dmat)

    return np.asarray(p, dtype=float)

def predict_worker_risk(age, heart_rate):
    try:
        # 입력값 구성: 학습 시 사용한 컬럼명과 일치해야 함
        hrmax = 220 - age if age is not None else 180
        hrmax = max(hrmax, 1)
        hr_ratio = heart_rate / hrmax

        input_df = pd.DataFrame([[
            int(age), float(heart_rate), float(hrmax), float(hr_ratio)
        ]], columns=["Age", "HR", "HRmax", "hr_ratio"])

        # 예측 수행
        pred = model.predict(input_df)[0]
        return int(pred)    # 0(정상), 1(위험)
    except Exception as e:
        print("예측 오류: ", e)
        return None