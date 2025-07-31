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
        # encoding='Latin1' 추가
        data = pickle.load(f, encoding='latin1')
    
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

def load_features(pkl_path: str, window_size: int = 30, step: int = 1):
    """
    PPG-DaLiA .pkl 파일에서 HR + ACC + Activity(활동 레이블) 기반
    멀티 피처(feature) 윈도우 벡터를 생성

    Parameters
    ----------
    pkl_path : str
        "PPG_FieldStudy/S1/S1.pkl" 형태의 파일 경로
    window_size : int
        윈도우 길이 (기본 30 -> 약 60초, HR label은 2초 간격)
    step : int
        슬라이딩 윈도우 이동 간격 (기본 1)
        한 번 예측한 후, 다음 예측에서는 바로 다음 값부터 시작 (59개 겹침 -> 학습 데이터 ↑)

    Returns
    -------
    features : np.ndarray
        shape = (N, window_size * 3)
        각 윈도우 = [HR 30개, ACC 30개, ACTIVITY 30개] -> 평탄화 (flatten)
    """

    # .pkl 파일 로드
    # encoding='latin1' : python2에서 저장된 pickle 호환용
    with open(pkl_path, "rb") as f:
        data = pickle.load(f, encoding="latin1")

    # HR label 시계열
    # label: ECG(심전도) 기반으로 계산된 심박수(HR)
    # ECG 신호에서 R-peak를 검출
    # 8초 길이의 구간으로 평균 HR을 계산
    # 2초씩 겹치도록 윈도우를 옮겨가면 HR을 산출 (6초는 겹치는 데이터)
    # 해당 8초 동안의 평균 HR
    hr_series = np.array(data['label'], dtype=float)

    # ACC: 32Hz(HR와 길이가 다름)
    acc = np.array(data['signal']['wrist']['ACC'])
    # 윈도우 단위로 평균 가속도 크기, 표준편차 등 통계 특성을 뽑음
    acc_magnitude = np.linalg.norm(acc, axis=1)

    # ACC와 HR길이를 맞춤
    acc_downsampled = np.interp(
        np.linspace(0, len(acc_magnitude) - 1, len(hr_series)),
        np.arange(len(acc_magnitude)),
        acc_magnitude
    )

    features = []
    for start in range(0, len(hr_series) - window_size + 1, step):
        hr_seg = hr_series[start:start + window_size]
        acc_seg = acc_downsampled[start:start + window_size]

        # 2개의 feature 채널 (HR, ACC)
        # [윈도우 길이, 2] -> flatten 해서 1D로 변환
        segment = np.stack([hr_seg, acc_seg], axis=1).flatten()
        features.append(segment)

    return np.array(features)