# health_monitoring/training/train_autoencoder.py
# PPG-DaLiA로 Autoencoder 학습 스크립트
from pathlib import Path
from sklearn.model_selection import train_test_split
from keras import layers
import numpy as np
import keras

# 데이터 로드
data_path = Path("health_monitoring/training/all_windows.npy")
windows = np.load(data_path)

# 정규화 (0 ~ 1)
windows = (windows - windows.min()) / (windows.max() - windows.min())

# Train/Validation split
train_data, val_data = train_test_split(windows, test_size=0.2, random_state=42)

print(f"Train shape: {train_data.shape}, Val shape: {val_data.shape}")

# Autoencoder 모델 정의
input_dim = train_data.shape[1]

model = keras.Sequential([
    layers.Input(shape=(input_dim,)),
    layers.Dense(16, activation='relu'),
    layers.Dense(8, activation='relu'),
    layers.Dense(16, activation='relu'),
    layers.Dense(input_dim, activation='linear')    # 원래 입력을 재구성
])

model.compile(optimizer='adam', loss='mse')
model.summary()

# 학습
history = model.fit(
    train_data,
    train_data,     # 입력 == 출력 (Autoencoder)
    epochs=30,
    batch_size=64,
    validation_data=(val_data, val_data)
)

# 모델 저장
model.save("health_monitoring/app/models/autoencoder_model.keras")

# Reconstruction error 계산
reconstructions = model.predict(val_data)
mse = np.mean(np.square(val_data - reconstructions), axis=1)

print("Reconstruction error (앞 10개):", mse[:10])