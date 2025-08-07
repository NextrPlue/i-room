import os
import json
import shutil
from pathlib import Path
import random

## 이미지 인스턴스 수 맞추기, train val 갯수 맞추기

# ==============================
# 1. 경로 설정
# ==============================
BASE_DIR  = os.path.normpath("C:/Users/kalin/Desktop/mass")
OUTPUT_DIR = os.path.normpath("C:/Users/kalin/Desktop/finished")

os.makedirs(OUTPUT_DIR, exist_ok=True)

# 사용할 클래스
CLASS_BELT = "01"   # 안전벨트
CLASS_HELMET = "07" # 안전모

# ==============================
# 2. 이미지/라벨 경로 지정
# ==============================
img_root = Path(BASE_DIR) / "train" / "images"
label_root = Path(BASE_DIR) / "train" / "labels_json"

# ==============================
# 3. 각 이미지별 안전벨트/안전모 인스턴스 수 집계
# ==============================
image_stats = {}
image_map = {img_path.name.lower(): img_path for img_path in img_root.glob("*.jpg")}

for label_file in label_root.glob("*.json"):
    with open(label_file, "r", encoding="utf-8") as f:
        data = json.load(f)

    anns = data.get("annotations", [])
    belt_count = sum(1 for ann in anns if ann.get("class") == CLASS_BELT)
    helmet_count = sum(1 for ann in anns if ann.get("class") == CLASS_HELMET)

    pure_filename = Path(data["image"]["filename"]).name.lower()

    if pure_filename not in image_map:
        print(f"⚠️ 매칭 실패 (이미지 없음): {pure_filename}")
        continue

    image_stats[pure_filename] = {"belt": belt_count, "helmet": helmet_count}

belt_images = [f for f, v in image_stats.items() if v["belt"] > 0]
helmet_images = [f for f, v in image_stats.items() if v["helmet"] > 0]

belt_only = list(set(belt_images) - set(helmet_images))
helmet_only = list(set(helmet_images) - set(belt_images))
both = list(set(helmet_images) & set(belt_images))

print("총 이미지 수:", len(image_stats))
print("안전벨트 이미지 수:", len(belt_images))
print("안전모 이미지 수:", len(helmet_images))
print("둘 다 포함된 이미지 수:", len(both))

# ==============================
# 4. split_dataset 함수
# ==============================
def split_dataset(belt_only, helmet_only, both, val_ratio=0.2):
    random.shuffle(belt_only)
    random.shuffle(helmet_only)
    random.shuffle(both)

    belt_val_size = int(len(belt_only) * val_ratio)
    helmet_val_size = int(len(helmet_only) * val_ratio)
    both_val_size = int(len(both) * val_ratio)

    val = belt_only[:belt_val_size] + helmet_only[:helmet_val_size] + both[:both_val_size]
    train = belt_only[belt_val_size:] + helmet_only[helmet_val_size:] + both[both_val_size:]

    return train, val

train_list, val_list = split_dataset(belt_only, helmet_only, both)

print("최종 train 이미지 수:", len(train_list))
print("최종 val 이미지 수:", len(val_list))

# ==============================
# 5. 결과 저장
# ==============================
def save_split(images, split_name):
    img_out = Path(OUTPUT_DIR) / split_name / "images"
    label_out = Path(OUTPUT_DIR) / split_name / "labels_json"
    img_out.mkdir(parents=True, exist_ok=True)
    label_out.mkdir(parents=True, exist_ok=True)

    for fname in images:
        # 이미지 복사
        shutil.copy2(img_root / fname, img_out / fname)

        # 라벨 파일명 처리
        stem_name = Path(fname).stem  
        label_file = label_root / f"{stem_name}.json"

        if not label_file.exists():
            alt_file = label_root / f"{fname}.json"
            if alt_file.exists():
                print(f"⚠️ 확장자 포함 라벨 발견: {alt_file.name}, 사용함")
                label_file = alt_file
            else:
                print(f"❌ 라벨 파일 없음: {stem_name}.json")
                continue

        shutil.copy2(label_file, label_out / label_file.name)

save_split(train_list, "train")
save_split(val_list, "val")

print("데이터셋 분할 완료!")

# ==============================
# 6. 파일 개수 확인
# ==============================
def count_files_in_split(split_name):
    split_path = Path(OUTPUT_DIR) / split_name
    img_count = len(list((split_path / "images").glob("*.jpg")))
    label_count = len(list((split_path / "labels_json").glob("*.json")))
    return img_count, label_count

train_imgs, train_labels = count_files_in_split("train")
val_imgs, val_labels = count_files_in_split("val")

print("\n=== 파일 개수 ===")
print(f"Train - images: {train_imgs}, labels_json: {train_labels}")
print(f"Val   - images: {val_imgs}, labels_json: {val_labels}")
