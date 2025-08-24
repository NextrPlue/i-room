import torch
import torch.nn as nn
import torch.nn.utils.prune as prune
from ultralytics import YOLO

# 모델 불러오기
MODEL_PATH = "best3.pt"
SAVE_PATH = "best3_structural_pruned.pt"
SPARSITY = 0.3  # 희소성 비율 (30%)

model = YOLO(MODEL_PATH)
device = "cuda" if torch.cuda.is_available() else "cpu"
model.to(device)
print(f" Using device: {device}")

# PyTorch 모델 객체
nn_model = model.model

# 프루닝 적용 (Conv 출력 채널 + Linear 뉴런 단위 제거)
total_layers = 0
for name, module in nn_model.named_modules():
    if isinstance(module, nn.Conv2d):
        # Conv 출력 채널 단위 구조적 프루닝
        prune.ln_structured(module, name="weight", amount=SPARSITY, n=2, dim=0)
        prune.remove(module, "weight")
        total_layers += 1
    elif isinstance(module, nn.Linear):
        # Linear 입력 뉴런 단위 구조적 프루닝
        prune.ln_structured(module, name="weight", amount=SPARSITY, n=2, dim=1)
        prune.remove(module, "weight")
        total_layers += 1

print(f"Conv/Linear 레이어 {total_layers}개에 {SPARSITY*100:.0f}% 구조적 프루닝 적용 완료")

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
    print(f"\n Global sparsity: {global_sparsity*100:.2f}%")
    return global_sparsity

global_sparsity = check_sparsity(nn_model)

# 프루닝 모델 저장 (YOLO API 호환)
ckpt = {
    "model": nn_model,
    "train_args": {},
}
torch.save(ckpt, SAVE_PATH)
print(f"Pruned 모델 저장 완료: {SAVE_PATH}")
