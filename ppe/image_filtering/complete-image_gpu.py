import os
import json
import shutil
import random
from collections import defaultdict

from PIL import Image
import numpy as np
from skimage.measure import shannon_entropy

import torch
import torchvision.transforms as T

# ================== 설정 ==================
base_dir = os.path.normpath("C:/Users/kalin/Desktop/extract")
input_root = os.path.join(base_dir, "filtered")
output_root = os.path.join(base_dir, "completed")
split_set = ["train", "val"]
target_classes = {"01", "03", "07"}
per_class_target_map = {"train": 180, "val": 30}

QUALITY_THRESHOLD = {
    'laplacian': 50,
    'snr': 5,
    'entropy': 2.5,
    'brightness_min': 20,
    'brightness_max': 240,
    'contrast': 10
}

RELAXED_THRESHOLD = {
    'laplacian': 20,
    'snr': 2,
    'entropy': 1.5,
    'brightness_min': 10,
    'brightness_max': 250,
    'contrast': 5
}

device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
# ==========================================

def is_high_quality(image_path, threshold):
    try:
        img = Image.open(image_path).convert("L")
        image_tensor = T.ToTensor()(img).to(device)  # [1, H, W]
        image_np = image_tensor.squeeze().cpu().numpy()

        # Laplacian (edge/sharpness)
        lap = torch.nn.functional.conv2d(
            image_tensor.unsqueeze(0),
            weight=torch.tensor([[[[0, 1, 0], [1, -4, 1], [0, 1, 0]]]], dtype=torch.float32).to(device),
            padding=1
        ).var().item()

        mean = image_tensor.mean().item()
        stddev = image_tensor.std().item()
        snr = mean / stddev if stddev > 0 else 0
        entropy = shannon_entropy(image_np)

        if lap < threshold['laplacian']: return False
        if snr < threshold['snr']: return False
        if entropy < threshold['entropy']: return False
        if not (threshold['brightness_min'] < mean * 255 < threshold['brightness_max']): return False
        if stddev * 255 < threshold['contrast']: return False

        return True
    except:
        return False

def remove_corrupted_images(images_dir):
    removed = 0
    for fname in os.listdir(images_dir):
        if not fname.lower().endswith((".jpg", ".jpeg", ".png")):
            continue
        path = os.path.join(images_dir, fname)
        try:
            with Image.open(path) as img:
                img.verify()
        except:
            os.remove(path)
            removed += 1
            print(f"❌ 삭제된 손상 이미지: {fname}")
    return removed

class_counts = defaultdict(int)

for split in split_set:
    print(f"\n🔄 {split.upper()} 데이터 처리 시작")
    per_class_target = per_class_target_map[split]

    in_labels_dir = os.path.join(input_root, split, "labels_json")
    in_images_dir = os.path.join(input_root, split, "images")
    out_labels_dir = os.path.join(output_root, split, "labels_json", split)
    out_images_dir = os.path.join(output_root, split, "images", split)
    os.makedirs(out_labels_dir, exist_ok=True)
    os.makedirs(out_images_dir, exist_ok=True)

    remove_corrupted_images(in_images_dir)

    selected = {cls: set() for cls in target_classes}
    candidates = []

    for json_file in os.listdir(in_labels_dir):
        if not json_file.endswith(".json"): continue
        json_path = os.path.join(in_labels_dir, json_file)
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        annotations = data.get("annotations", [])
        classes = {ann.get("class") for ann in annotations}
        valid = classes & target_classes
        if not valid: continue

        filename = data.get("image", {}).get("filename", "")
        if not filename or not os.path.exists(os.path.join(in_images_dir, filename)):
            continue

        candidates.append((json_file, filename, valid))

    print(f"✅ 후보 수: {len(candidates)}")
    random.shuffle(candidates)

    def filter_candidates(threshold):
        for json_file, filename, class_set in candidates:
            if not os.path.exists(os.path.join(in_images_dir, filename)):
                continue
            if is_high_quality(os.path.join(in_images_dir, filename), threshold):
                for cls in class_set:
                    if cls in target_classes and len(selected[cls]) < per_class_target:
                        selected[cls].add((json_file, filename))
                        break

    filter_candidates(QUALITY_THRESHOLD)
    for cls in target_classes:
        if len(selected[cls]) < per_class_target:
            print(f"⚠️ 클래스 {cls} 부족 → 기준 완화 시도")
            filter_candidates(RELAXED_THRESHOLD)

    for cls, files in selected.items():
        for json_file, fname in list(files)[:per_class_target]:
            src_img = os.path.join(in_images_dir, fname)
            dst_img = os.path.join(out_images_dir, fname)
            dst_json = os.path.join(out_labels_dir, json_file)

            if not os.path.exists(os.path.dirname(dst_img)):
                os.makedirs(os.path.dirname(dst_img), exist_ok=True)
            if not os.path.exists(os.path.dirname(dst_json)):
                os.makedirs(os.path.dirname(dst_json), exist_ok=True)

            shutil.copy2(src_img, dst_img)
            shutil.copy2(os.path.join(in_labels_dir, json_file), dst_json)
            class_counts[cls] += 1

print("\n📊 최종 결과")
for cls in sorted(class_counts):
    print(f"클래스 {cls}: {class_counts[cls]}장")
print(f"총 저장 이미지 수: {sum(class_counts.values())}장")
