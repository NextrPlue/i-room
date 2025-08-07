# health/utils/model_utils.py

import pickle
import numpy as np
import pandas as pd
import os

# 모델 경로 정의
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODEL_PATH = os.path.join(BASE_DIR, "models", "lgb_model_2.pkl")

# 모델 로딩
with open(MODEL_PATH, "rb") as f:
    model = pickle.load(f)

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