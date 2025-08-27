import os
import cv2
import json
import shutil
import random
import numpy as np
from PIL import Image
from collections import defaultdict
from skimage.measure import shannon_entropy
from concurrent.futures import ThreadPoolExecutor, as_completed

# ================== 설정 ==================
base_dir = os.path.normpath("C:/Users/kalin/Desktop/1th_completed")
input_root = os.path.join(base_dir, "오피스_filtered")
output_root = os.path.join(base_dir, "오피스_2th_completed")
split_set = ["train", "val"]
target_classes = {"01", "03", "07"}

# 클래스당 목표 수량 (split마다 다름)
per_class_target_map = {
    "train": 700,
    "val": 150,
}

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
# ==========================================

def remove_corrupted_images(images_dir):
    removed = 0
    for fname in os.listdir(images_dir):
        if not fname.lower().endswith((".jpg", ".jpeg", ".png")):
            continue
        path = os.path.join(images_dir, fname)
        try:
            with Image.open(path) as img:
                img.verify()
        except Exception:
            os.remove(path)
            removed += 1
            print(f"삭제된 손상 이미지: {fname}")
    return removed

def is_high_quality(image_path, threshold):
    try:
        image = Image.open(image_path).convert("L").resize((512, 512))
        image_np = np.array(image)

        lap = cv2.Laplacian(image_np, cv2.CV_64F).var()
        mean = np.mean(image_np)
        stddev = np.std(image_np)
        snr = mean / stddev if stddev > 0 else 0
        entropy = shannon_entropy(image_np)

        if lap < threshold['laplacian']: return False
        if snr < threshold['snr']: return False
        if entropy < threshold['entropy']: return False
        if not (threshold['brightness_min'] < mean < threshold['brightness_max']): return False
        if stddev < threshold['contrast']: return False

        return True
    except Exception:
        return False

def evaluate_candidate(args):
    json_file, filename, class_set, threshold, in_images_dir = args
    img_path = os.path.join(in_images_dir, filename)
    if not os.path.exists(img_path):
        return None
    if is_high_quality(img_path, threshold):
        return (json_file, filename, class_set)
    return None

# 전체 클래스별 카운트 저장용
class_counts = defaultdict(int)

for split in split_set:
    print(f"\n🔄 {split.upper()} 데이터 처리 시작")

    per_class_target = per_class_target_map[split]

    in_labels_dir = os.path.join(input_root, split, "labels_json")
    in_images_dir = os.path.join(input_root, split, "images")
    out_labels_dir = os.path.join(output_root, split, "labels_json")
    out_images_dir = os.path.join(output_root, split, "images")
    os.makedirs(out_labels_dir, exist_ok=True)
    os.makedirs(out_images_dir, exist_ok=True)

    remove_corrupted_images(in_images_dir)

    selected = {cls: set() for cls in target_classes}
    candidates = []

    # 후보 목록 생성
    for json_file in os.listdir(in_labels_dir):
        if not json_file.endswith(".json"):
            continue
        json_path = os.path.join(in_labels_dir, json_file)
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        annotations = data.get("annotations", [])
        classes = {ann.get("class") for ann in annotations}
        valid = classes & target_classes
        if not valid:
            continue

        filename = data.get("image", {}).get("filename", "")
        img_path = os.path.join(in_images_dir, filename)
        if not filename or not os.path.exists(img_path):
            continue

        candidates.append((json_file, filename, valid))

    print(f"후보 수: {len(candidates)}")
    random.shuffle(candidates)

    def parallel_filter(threshold):
        args_list = [(json_file, filename, class_set, threshold, in_images_dir)
                     for json_file, filename, class_set in candidates]

        with ThreadPoolExecutor(max_workers=8) as executor:
            for future in as_completed([executor.submit(evaluate_candidate, args) for args in args_list]):
                result = future.result()
                if not result:
                    continue
                json_file, filename, class_set = result

                for cls in class_set:
                    if cls in target_classes and len(selected[cls]) < per_class_target:
                        selected[cls].add((json_file, filename))
                        break

    # 기본 필터 적용
    parallel_filter(QUALITY_THRESHOLD)

    # 부족 시 완화 기준 적용
    for cls in target_classes:
        if len(selected[cls]) < per_class_target:
            print(f"클래스 {cls} 부족 → 기준 완화")
            parallel_filter(RELAXED_THRESHOLD)

    # 최종 복사 (클래스별 폴더 포함)
    for cls, files in selected.items():
        count = min(len(files), per_class_target)
        for json_file, fname in list(files)[:count]:
            src_img = os.path.join(in_images_dir, fname)
            src_json = os.path.join(in_labels_dir, json_file)

            # 클래스별 서브디렉토리 경로
            dst_img = os.path.join(out_images_dir, cls, fname)
            dst_json = os.path.join(out_labels_dir, cls, json_file)

            if not os.path.exists(src_img):
                continue
            os.makedirs(os.path.dirname(dst_img), exist_ok=True)
            os.makedirs(os.path.dirname(dst_json), exist_ok=True)

            shutil.copy(src_img, dst_img)
            shutil.copy(src_json, dst_json)
            class_counts[f"{split}_{cls}"] += 1

# 결과 출력
print("\n최종 결과")
for key in sorted(class_counts.keys()):
    print(f"{key}: {class_counts[key]}장")
print(f"총 저장 이미지 수: {sum(class_counts.values())}장")
