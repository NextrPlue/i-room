# health_monitoring/training/preprocess_dataset.py
# PPG-DaLiA HR 추출 전처리
from health_monitoring.app.utils.preprocessing import load_features
from pathlib import Path
import numpy as np

# 데이터 경로
data_root = Path("C:/Users/minsu/Desktop/ppg+dalia/data/PPG_FieldStudy")
output_path = Path("health_monitoring/training/all_features_windows.npy")

# 사용할 참가자 ID
subjects = [f"S{i}" for i in range(1, 16)]  # S1 ~ S15

# 모든 참가자의 데이터를 담을 리스트
all_windows = []

for subject_id in subjects:
    # 각 참가자의 데이터 경로
    pkl_path = data_root / subject_id / f"{subject_id}.pkl"
    
    if not pkl_path.exists():
        print(f"[ERROR] {subject_id} 데이터 없음, 건너뜀")
        continue

    print(f"처리 중: {subject_id}")

    # HR + ACC + Activity 기반 feature 윈도우 생성
    features = load_features(pkl_path, window_size=30, step=1)

    all_windows.append(features) # 리스트에 저장

# 전체 데이터 합치기
all_windows = np.vstack(all_windows)
print("전체 윈도우 shape:", all_windows.shape)

# 저장
np.save(output_path, all_windows)
print(f"저장 완료: {output_path}")