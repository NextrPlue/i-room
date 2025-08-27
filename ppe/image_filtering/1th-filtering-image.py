import os
import zipfile
import json
import shutil

# 경로 설정 (역슬래시 이스케이프 또는 슬래시 사용)
base_dir = os.path.normpath("C:/Users/kalin/Desktop/1th_completed")
zip_path = os.path.normpath("C:/Users/kalin/Desktop/사용데이터")

os.makedirs(base_dir, exist_ok=True)

# 압축 해제 함수
def unzip_files(zip_file_path, target_dir):
    with zipfile.ZipFile(zip_file_path, 'r') as zip_ref:
        zip_ref.extractall(target_dir)
    print(f"압축 해제 완료: {zip_file_path} -> {target_dir}")

# 압축 파일명
file_name_images = "공항-images.zip"             # ex) "8.오피스_문정동_KG_사옥_신축공사_images.zip"
file_name_labels_json = "공항-labels_json.zip"  # ex) "8.오피스_문정동_KG_사옥_신축공사_labels_json.zip"

# 압축 해제
unzip_files(os.path.join(zip_path, file_name_images), os.path.join(base_dir, "images"))
unzip_files(os.path.join(zip_path, file_name_labels_json), os.path.join(base_dir, "labels_json"))

# 클래스 필터링 설정
target_classes = {"01", "03", "07"}    # 안전벨트, 안전고리, 안전모

# train/val 두 개의 데이터셋을 각각 처리
for split in ["train", "val"]:
    # 라벨(JSON) 디렉토리 경로: base_dir/labels_json/train 또는 val
    labels_dir = os.path.join(base_dir, "labels_json", split)
    
    # 이미지 디렉토리 경로: base_dir/images/train 또는 val
    images_dir = os.path.join(base_dir, "images", split)

    # 필터링된 라벨 저장 경로: base_dir/filtered/train/labels_json 또는 val
    filtered_labels_dir = os.path.join(base_dir, "filtered", split, "labels_json")
    
    # 필터링된 이미지 저장 경로: base_dir/filtered/train/images 또는 val
    filtered_images_dir = os.path.join(base_dir, "filtered", split, "images")

    # 디렉토리 생성 (이미 존재하면 무시)
    os.makedirs(filtered_labels_dir, exist_ok=True)
    os.makedirs(filtered_images_dir, exist_ok=True)

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

        # target_classes(예: 안전모, 안전고리 등)에 해당하는 객체가 하나라도 있는지 확인
        contains_target_class = any(
            ann.get("class") in target_classes for ann in annotations
        )

        # 만약 타겟 클래스가 없으면 해당 JSON/이미지 건너뜀
        if not contains_target_class:
            continue

        # 이미지 정보 중 filename 값 가져오기
        image_info = data.get("image", {})
        filename = image_info.get("filename")

        # filename이 없으면 건너뜀 (예외 상황)
        if not filename:
            print(f"⚠️ filename 없음 in {json_file}")
            continue

        # 원본 이미지 경로와 복사 대상 경로 설정
        src_img_path = os.path.join(images_dir, filename)
        dst_img_path = os.path.join(filtered_images_dir, filename)

        # 실제 이미지가 존재할 경우에만 JSON과 이미지 모두 복사
        if os.path.exists(src_img_path):
            # JSON 복사
            shutil.copy(json_path, os.path.join(filtered_labels_dir, json_file))
            # 이미지 복사
            shutil.copy(src_img_path, dst_img_path)
        else:
            # 이미지가 없을 경우 경고 출력
            print(f"이미지 없음: {src_img_path}")

print("원본 데이터 개수 확인")

for split in ["train", "val"]:
    original_labels_dir = os.path.join(base_dir, "labels_json", split)
    original_images_dir = os.path.join(base_dir, "images", split)

    num_labels = len([f for f in os.listdir(original_labels_dir) if f.endswith(".json")])
    num_images = len([f for f in os.listdir(original_images_dir) if f.lower().endswith(('.jpg', '.png'))])

    print(f"[{split.upper()}] 원본 JSON 수: {num_labels}")
    print(f"[{split.upper()}] 원본 이미지 수: {num_images}")

# 전체 합계 출력
total_original_labels = 0
total_original_images = 0
for split in ["train", "val"]:
    total_original_labels += len([
        f for f in os.listdir(os.path.join(base_dir, "labels_json", split))
        if f.endswith(".json")
    ])
    total_original_images += len([
        f for f in os.listdir(os.path.join(base_dir, "images", split))
        if f.lower().endswith(('.jpg', '.png'))
    ])

print(f"\n전체 원본 JSON 수: {total_original_labels}")
print(f"전체 원본 이미지 수: {total_original_images}")

# 필터링된 결과 개수 출력
for split in ["train", "val"]:
    filtered_labels_dir = os.path.join(base_dir, "filtered", split, "labels_json")
    filtered_images_dir = os.path.join(base_dir, "filtered", split, "images")

    num_labels = len([f for f in os.listdir(filtered_labels_dir) if f.endswith(".json")])
    num_images = len([f for f in os.listdir(filtered_images_dir) if f.lower().endswith(('.jpg', '.png'))])

    print(f"[{split.upper()}] 필터링된 JSON 수: {num_labels}")
    print(f"[{split.upper()}] 필터링된 이미지 수: {num_images}")

# 전체 합계 출력
total_labels = 0
total_images = 0
for split in ["train", "val"]:
    filtered_labels_dir = os.path.join(base_dir, "filtered", split, "labels_json")
    filtered_images_dir = os.path.join(base_dir, "filtered", split, "images")

    total_labels += len([f for f in os.listdir(filtered_labels_dir) if f.endswith(".json")])
    total_images += len([f for f in os.listdir(filtered_images_dir) if f.lower().endswith(('.jpg', '.png'))])

print("\n전체 필터링 결과")
print(f"총 JSON 수: {total_labels}")
print(f"총 이미지 수: {total_images}")

from collections import defaultdict

# 클래스별 등장 횟수 저장용 딕셔너리
class_counts = defaultdict(int)

# filtered JSON에서 class 값 집계
for split in ["train", "val"]:
    filtered_labels_dir = os.path.join(base_dir, "filtered", split, "labels_json")
    
    for json_file in os.listdir(filtered_labels_dir):
        if not json_file.endswith(".json"):
            continue

        json_path = os.path.join(filtered_labels_dir, json_file)

        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        annotations = data.get("annotations", [])
        for ann in annotations:
            cls = ann.get("class")
            if cls in target_classes:
                class_counts[cls] += 1

# 결과 출력
print("\n클래스별 객체 수 집계 (filtered 결과 기준):")
for cls in sorted(class_counts):
    print(f"클래스 {cls}: {class_counts[cls]}개")