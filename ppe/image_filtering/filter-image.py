import os
import json
import shutil
from collections import defaultdict

# 클래스에 맞는 이미지와 json을 가져오는 코드임(둘중 하나라도 쌍이 안맞으면 삭제하는 코드)

# ✅ 경로 설정 (바탕화면의 'target' 폴더에 train/val 있음)
base_dir = os.path.normpath("C:/Users/kalin/Desktop/target")
filtered_base = os.path.join(base_dir, "filtered")

# ✅ 필터링할 클래스 지정
target_classes = {"01", "07"}

# ✅ train/val 데이터 처리
for split in ["train", "val"]:
    labels_dir = os.path.join(base_dir, split, "labels_json")
    images_dir = os.path.join(base_dir, split, "images")

    filtered_labels_dir = os.path.join(filtered_base, split, "labels_json")
    filtered_images_dir = os.path.join(filtered_base, split, "images")

    os.makedirs(filtered_labels_dir, exist_ok=True)
    os.makedirs(filtered_images_dir, exist_ok=True)

    class_counts = defaultdict(int)

    for json_file in os.listdir(labels_dir):
        if not json_file.endswith(".json"):
            continue

        json_path = os.path.join(labels_dir, json_file)
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        annotations = data.get("annotations", [])
        image_info = data.get("image", {})
        filename = image_info.get("filename")

        # 해당 JSON에 대상 클래스가 포함되어 있는지 확인
        contains_target = any(ann.get("class") in target_classes for ann in annotations)

        if contains_target and filename:
            for ann in annotations:
                cls = ann.get("class")
                if cls in target_classes:
                    class_counts[cls] += 1

            # JSON 및 이미지 파일 복사
            shutil.copy(json_path, os.path.join(filtered_labels_dir, json_file))

            src_img_path = os.path.join(images_dir, filename)
            dst_img_path = os.path.join(filtered_images_dir, filename)

            if os.path.exists(src_img_path):
                shutil.copy(src_img_path, dst_img_path)
            else:
                print(f"⚠️ 이미지 없음: {src_img_path}")

    # ✅ 클래스별 필터링 수 출력
    print(f"\n📂 {split.upper()} - 클래스별 객체 수:")
    for cls in sorted(class_counts):
        print(f"클래스 {cls}: {class_counts[cls]}개")

# ✅ 최종 필터링된 결과 개수 출력
for split in ["train", "val"]:
    filtered_labels_dir = os.path.join(filtered_base, split, "labels_json")
    filtered_images_dir = os.path.join(filtered_base, split, "images")

    num_labels = len([f for f in os.listdir(filtered_labels_dir) if f.endswith(".json")])
    num_images = len([f for f in os.listdir(filtered_images_dir) if f.lower().endswith(('.jpg', '.png'))])

    print(f"\n📊 {split.upper()} 필터링된 JSON 수: {num_labels}")
    print(f"📊 {split.upper()} 필터링된 이미지 수: {num_images}")
