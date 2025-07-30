# health_monitoring/app/utils/preprocessing.py
import pickle
from pathlib import Path
import numpy as np

def load_hr_series(pkl_path: str) -> np.ndarray:
    """
    PPG-DaLiA .pkl 파일에서 HR(label) 시계열을 로드
    pkl_path: "PPG_FieldStudy/S1/S1.pkl"
    return: numpy array (HR bpm)
    """
    with open(pkl_path, "rb") as f:
        data = pickle.load(f)
    
    return np.array(data['label'], dtype=float)

def sliding_window(series: np.ndarray, window_size: int = 30, step: int = 1):
    """
    시계열 데이터를 window_size 길이의 윈도우로 잘라서 반환
    (2초 간격 데이터임을 유의: 30개 윈도우 = 60초)
    """
    windows = []
    for start in range(0, len(series) - window_size + 1, step):
        segment = series[start:start+window_size]
        windows.append(segment)

    return np.array(windows)
    