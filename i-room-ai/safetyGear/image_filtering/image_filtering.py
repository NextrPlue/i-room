import os
import zipfile
import json
import shutil

"""
⚠️⚠️⚠️ 실행 전 주의사항 ⚠️⚠️⚠️
1. 아래 변수는 본인 환경에 맞게 정의해야함.
INPUT_PATH: Raw Data 위치
OUTPUT_PATH: Raw Data 압축 해제 및 Filtering 결과 위치
FILE_NAME: Raw Data 압축 파일 이름 (.zip 제외)

2. 압축 파일은 다음과 같은 폴더 구조로 정리되어 있어야함.
xxx.zip/
├── train/
│   ├── images/
│   │   └── xxx.jpg
│   └── labels_json/
│       └── xxx.json
└── val/
    ├── images/
    │   └── xxx.jpg
    └── labels_json/
        └── xxx.json
"""
INPUT_PATH = "D:/"
OUTPUT_PATH = "D:/"
FILE_NAME = "3.공장_가산동_A1타워_지식산업_센터2"

# 경로 설정
input_path = os.path.normpath(INPUT_PATH)
output_path = os.path.normpath(OUTPUT_PATH)

# 압축 파일명 설정
zip_file = FILE_NAME + ".zip"

# 압축 해제 함수
def unzip_files(input_path, output_path):
    with zipfile.ZipFile(input_path, 'r') as zip_ref:
        zip_ref.extractall(output_path)
    print(f"압축 해제 완료: {input_path} -> {output_path}")

# 압축 해제
# unzip_files(os.path.join(input_path, zip_file), os.path.join(output_path, FILE_NAME))

# 클래스 필터링 설정
wear_classes = {"01", "03", "07"}       # 안전벨트, 안전고리, 안전모 착용
not_wear_classes = {"02", "04", "08"}   # 안전벨트, 안전고리, 안전모 미착용: Background Image로 사용

# train, val 두 개의 데이터셋을 각각 처리
for split in ["train", "val"]:
    # 라벨(JSON) 폴더 경로: output_path/FILE_NAME/train, val/labels_json
    labels_dir = os.path.join(output_path, FILE_NAME, split, "labels_json")
    
    # 이미지 폴더 경로: output_path/FILE_NAME/train, val/images
    images_dir = os.path.join(output_path, FILE_NAME, split, "images")

    # target, background 폴더 준비
    target_labels = os.path.join(output_path, "filtered", "target", split, "labels_json")
    target_images = os.path.join(output_path, "filtered", "target", split, "images")
    background_labels = os.path.join(output_path, "filtered", "background", split, "labels_json")
    background_images = os.path.join(output_path, "filtered", "background", split, "images")

    # 디렉토리 생성 (이미 존재하면 무시)
    for d in [target_labels, target_images, background_labels, background_images]:
        os.makedirs(d, exist_ok=True)

    # JSON 라벨 파일들을 순회
    for json_file in os.listdir(labels_dir):
        # .json 확장자가 아니면 건너뜀
        if not json_file.endswith(".json"):
            continue

        # JSON 파일 경로 설정
        json_path = os.path.join(labels_dir, json_file)

        # JSON 파일 열기 및 파싱
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        # annotations 리스트 가져오기 (객체 정보가 들어 있음)
        annotations = data.get("annotations", [])

        # 이미지에 존재하는 클래스 정보
        classes_in_img = {ann.get("class") for ann in annotations}

        # 착용, 미착용 분류
        has_wear = any(cls in wear_classes for cls in classes_in_img)
        has_not_wear = any(cls in not_wear_classes for cls in classes_in_img)

        # 1) 착용 클래스가 하나라도 있으면 -> target
        # 2) 착용 클래스는 없고, 미착용만 있으면 -> background
        if has_wear:
            copy_label_dir = target_labels
            copy_image_dir = target_images
        elif (not has_wear) and has_not_wear:
            copy_label_dir = background_labels
            copy_image_dir = background_images
        else:
            continue    # 둘 다 없으면 스킵

        # 이미지 정보 중 filename 값 가져오기
        filename = data.get("image", {}).get("filename")

        # filename이 없으면 건너뜀 (예외 상황)
        if not filename:
            print(f"⚠️ {json_file}: filename 값 없음")
            continue

        # 원본 이미지 경로와 복사 대상 경로 설정
        src_img_path = os.path.join(images_dir, filename)
        dst_img_path = os.path.join(copy_image_dir, filename)

        # ✅ 실제 이미지가 존재할 경우에만 JSON과 이미지 모두 복사
        if os.path.exists(src_img_path):
            shutil.copy(json_path, os.path.join(copy_label_dir, json_file))
            shutil.copy(src_img_path, dst_img_path)
        else:
            print(f"❌ 라벨은 있으나, {src_img_path} 이미지가 없음")

from collections import defaultdict

print("\n*** 원본 데이터 개수 확인 ***")
for split in ["train", "val"]:
    original_labels_dir = os.path.join(output_path, FILE_NAME, split, "labels_json")
    original_images_dir = os.path.join(output_path, FILE_NAME, split, "images")

    num_labels = len([f for f in os.listdir(original_labels_dir) if f.endswith(".json")])
    num_images = len([f for f in os.listdir(original_images_dir)
                      if f.lower().endswith(('.jpg', '.png', '.jpeg'))])

    print(f"[{split.upper()}] 원본 JSON 수: {num_labels}")
    print(f"[{split.upper()}] 원본 이미지 수: {num_images}")

# 전체 합계
total_original_labels = sum(
    len([f for f in os.listdir(os.path.join(output_path, FILE_NAME, split, "labels_json"))
         if f.endswith(".json")])
    for split in ["train", "val"]
)
total_original_images = sum(
    len([f for f in os.listdir(os.path.join(output_path, FILE_NAME, split, "images"))
         if f.lower().endswith(('.jpg', '.png', '.jpeg'))])
    for split in ["train", "val"]
)

print(f"\n총 원본 JSON 수: {total_original_labels}")
print(f"총 원본 이미지 수: {total_original_images}")


print("\n*** 필터링된 데이터 개수 확인 ***")
def count_files(base_dir):
    return len([f for f in os.listdir(base_dir) if os.path.isfile(os.path.join(base_dir, f))])

total_target_labels = 0
total_target_images = 0
total_background_labels = 0
total_background_images = 0

for split in ["train", "val"]:
    target_labels_dir = os.path.join(output_path, "filtered", "target", split, "labels_json")
    target_images_dir = os.path.join(output_path, "filtered", "target", split, "images")
    background_labels_dir = os.path.join(output_path, "filtered", "background", split, "labels_json")
    background_images_dir = os.path.join(output_path, "filtered", "background", split, "images")

    num_target_labels = count_files(target_labels_dir)
    num_target_images = count_files(target_images_dir)
    num_background_labels = count_files(background_labels_dir)
    num_background_images = count_files(background_images_dir)

    print(f"[{split.upper()}] target JSON 수: {num_target_labels}")
    print(f"[{split.upper()}] target 이미지 수: {num_target_images}")
    print(f"[{split.upper()}] background JSON 수: {num_background_labels}")
    print(f"[{split.upper()}] background 이미지 수: {num_background_images}")

    total_target_labels += num_target_labels
    total_target_images += num_target_images
    total_background_labels += num_background_labels
    total_background_images += num_background_images

# 전체 합계 출력
print("\n--- 전체 합계 ---")
print(f"전체 target JSON 수: {total_target_labels}")
print(f"전체 target 이미지 수: {total_target_images}")
print(f"전체 background JSON 수: {total_background_labels}")
print(f"전체 background 이미지 수: {total_background_images}")


# ======================
# 클래스별 객체 수 집계
# ======================
def count_classes(base_dir, classes_set):
    class_counts = defaultdict(int)
    for json_file in os.listdir(base_dir):
        if not json_file.endswith(".json"):
            continue
        json_path = os.path.join(base_dir, json_file)
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        for ann in data.get("annotations", []):
            cls = ann.get("class")
            if cls in classes_set:
                class_counts[cls] += 1
    return class_counts

wear_classes = {"01", "03", "07"}
not_wear_classes = {"02", "04", "08"}

target_class_counts = defaultdict(int)
background_class_counts = defaultdict(int)

for split in ["train", "val"]:
    # target
    target_labels_dir = os.path.join(output_path, "filtered", "target", split, "labels_json")
    tmp_counts = count_classes(target_labels_dir, wear_classes | not_wear_classes)
    for k, v in tmp_counts.items():
        target_class_counts[k] += v

    # background
    background_labels_dir = os.path.join(output_path, "filtered", "background", split, "labels_json")
    tmp_counts = count_classes(background_labels_dir, wear_classes | not_wear_classes)
    for k, v in tmp_counts.items():
        background_class_counts[k] += v

# 출력
print("\n*** 클래스별 객체 수 집계 (target) ***")
for cls in sorted(target_class_counts.keys()):
    print(f"클래스 {cls}: {target_class_counts[cls]}개")

print("\n*** 클래스별 객체 수 집계 (background) ***")
for cls in sorted(background_class_counts.keys()):
    print(f"클래스 {cls}: {background_class_counts[cls]}개")