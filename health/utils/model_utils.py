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