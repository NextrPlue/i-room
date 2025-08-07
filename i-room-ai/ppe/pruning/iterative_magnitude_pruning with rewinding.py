import torch
import torch.nn as nn
import torch.nn.utils.prune as prune
from ultralytics import YOLO
import copy

# 모델 불러오기
MODEL_PATH = "best3.pt"
SAVE_PATH = "best3_imp_pruned.pt"
TARGET_SPARSITY = 0.5  # 최종 희소성 목표 (50%)
IMP_STEPS = 3          # IMP 반복 횟수
EPOCHS_PER_STEP = 3    # 단계별 재학습 epoch (예시)

model = YOLO(MODEL_PATH)
device = "cuda" if torch.cuda.is_available() else "cpu"
model.to(device)
print(f"📌 Using device: {device}")

# PyTorch 모델 객체
nn_model = model.model
initial_state = copy.deepcopy(nn_model.state_dict())  # 초기 가중치 저장 (리와인딩용)

# sparsity 측정 함수
def check_sparsity(model):
    total_params, total_zeros = 0, 0
    for name, module in model.named_modules():
        if isinstance(module, (nn.Conv2d, nn.Linear)):
            weight = module.weight.data
            zeros = torch.sum(weight == 0).item()
            total = weight.numel()
            rate = zeros / total
            print(f"{name:<30} sparsity: {rate*100:.2f}%")
            total_params += total
            total_zeros += zeros
    global_sparsity = total_zeros / total_params
    print(f"\n📊 Global sparsity: {global_sparsity*100:.2f}%")
    return global_sparsity

# Iterative Pruning with Rewinding
for step in range(IMP_STEPS):
    print(f"\n========== IMP Step {step+1}/{IMP_STEPS} ==========")

    prune_rate = TARGET_SPARSITY / IMP_STEPS  # 단계별 프루닝 비율
    total_layers = 0

    # 프루닝 적용
    for name, module in nn_model.named_modules():
        if isinstance(module, (nn.Conv2d, nn.Linear)):
            prune.l1_unstructured(module, name="weight", amount=prune_rate)
            prune.remove(module, "weight")
            total_layers += 1

    print(f"Conv/Linear 레이어 {total_layers}개에 {prune_rate*100:.0f}% 프루닝 적용 완료")

    # sparsity 측정
    global_sparsity = check_sparsity(nn_model)

    # 가중치 리와인딩
    nn_model.load_state_dict(initial_state)
    print("🔄 가중치 리와인딩 완료")

    # 재훈련 (데이터셋 필요)
    # 실제 학습할 땐 data.yaml 경로 수정 필수
    model.model = nn_model
    model.train(data="data.yaml", epochs=EPOCHS_PER_STEP, imgsz=640, device=device)

# 프루닝 모델 저장 (YOLO API 호환)
ckpt = {
    "model": nn_model,
    "train_args": {},
}
torch.save(ckpt, SAVE_PATH)
print(f"\n💾 최종 IMP + Rewinding 모델 저장 완료: {SAVE_PATH}")
