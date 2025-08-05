# health_monitoring/utils/model_utils.py

import pickle
import numpy as np
import os

# 모델 경로 정의
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.join(BASE_DIR, "models", "lgb_model_2.pkl")

# 모델 로딩
with open(MODEL_PATH, "rb") as f:
    model = pickle.load(f)

def predict_worker_risk(age, heart_rate, step_per_minute, speed, pace, steps):
    """
    근로자 건강 이상 분류 함수
    입력값은 모두 숫자형 (float or int)
    """
    input_vector = np.array([[age, heart_rate, step_per_minute, speed, pace, steps]])
    pred = model.predict(input_vector)[0]
    return int(pred)    # 0(정상), 1(위험)