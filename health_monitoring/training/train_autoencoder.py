# health_monitoring/training/train_autoencoder.py
# PPG-DaLiA로 Autoencoder 학습 스크립트
from pathlib import Path
from sklearn.model_selection import train_test_split
from keras import layers
import numpy as np

# 데이터 로드
data_path = Path("health_monitoring/training/all_windows.npy")
windows = np.load(data_path)

# 정규화 (0 ~ 1)
windows = (windows - windows.min()) / (windows.max() - windows.min())

# Train/Validation split
train_data, val_data = train_test_split(windows, test_size=0.2, random_state=42)

print(f"Train shape: {train_data.shape}, Val shape: {val_data.shape}")