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

BASE_DIR = "D:/"
FOLDER_NAME = "6.상업시설_신사동_복합_시설"

# ================== 설정 ==================
base_dir = os.path.normpath(BASE_DIR)
input_root = os.path.join(base_dir, FOLDER_NAME + "_filtered")
output_root = os.path.join(base_dir, FOLDER_NAME + "_completed")
split_set = ["train", "val"]
target_classes = {"01", "03", "07"}

# 클래스당 목표 수량 (split마다 다름)
per_class_target_map = {
    "train": 180,
    "val": 30
}

# 이미지 품질 필터링 기준 (엄격)
QUALITY_THRESHOLD = {
    'laplacian': 50,        # 선명도
    'snr': 5,               # 신호대잡음비
    'entropy': 2.5,         # 정보량
    'brightness_min': 20,   # 최소 밝기
    'brightness_max': 240,  # 최대 밝기
    'contrast': 10          # 명암 대비
}

# 품질 기준 완화값 (충족 이미지 부족 시 사용)
RELAXED_THRESHOLD = {
    'laplacian': 20,
    'snr': 2,
    'entropy': 1.5,
    'brightness_min': 10,
    'brightness_max': 250,
    'contrast': 5
}
# ==========================================

# 손상된 이미지 제거
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
            print(f"❌ 삭제된 손상 이미지: {fname}")
    return removed

# 주어진 이미지가 품질 기준을 만족하는 지 확인
def is_high_quality(image_path, threshold):
    try:
        image = Image.open(image_path).convert("L").resize((512, 512))
        image_np = np.array(image)

        # 품질 지표 계산
        lap = cv2.Laplacian(image_np, cv2.CV_64F).var() # 선명도
        mean = np.mean(image_np)                        # 밝기
        stddev = np.std(image_np)                       # 밝기 표준편차
        snr = mean / stddev if stddev > 0 else 0        # SNR
        entropy = shannon_entropy(image_np)             # 엔트로피

        # 조건 만족 여부 확인
        if lap < threshold['laplacian']: return False
        if snr < threshold['snr']: return False
        if entropy < threshold['entropy']: return False
        if not (threshold['brightness_min'] < mean < threshold['brightness_max']): return False
        if stddev < threshold['contrast']: return False

        return True
    except Exception:
        return False

# 하나의 후보 이미지에 대해 품질 평가
def evaluate_candidate(args):
    json_file, filename, class_set, threshold, in_images_dir = args
    img_path = os.path.join(in_images_dir, filename)
    if not os.path.exists(img_path):
        return None
    if is_high_quality(img_path, threshold):
        return (json_file, filename, class_set)
    return None

# 클래스별 이미지 수 집계
class_counts = defaultdict(int)

# train / val 각각 처리
for split in split_set:
    print(f"\n🔄 {split.upper()} 데이터 처리 시작")

    # 현재 split(train / val)에 해당하는 목표 수량(180, 30)
    per_class_target = per_class_target_map[split]

    # 입출력 디렉토리 설정
    in_labels_dir = os.path.join(input_root, split, "labels_json")
    in_images_dir = os.path.join(input_root, split, "images")
    out_labels_dir = os.path.join(output_root, split, "labels_json")
    out_images_dir = os.path.join(output_root, split, "images")
    os.makedirs(out_labels_dir, exist_ok=True)
    os.makedirs(out_images_dir, exist_ok=True)

    # 손상된 이미지 제거
    remove_corrupted_images(in_images_dir)

    # 클래스별로 선택된 파일 저장용 선언
    selected = {cls: set() for cls in target_classes}
    candidates = []

    # 클래스별 후보 수 저장용 딕셔너리
    candidate_class_counts = defaultdict(int)

    # 후보 이미지 탐색
    for json_file in os.listdir(in_labels_dir):
        if not json_file.endswith(".json"):
            continue
        json_path = os.path.join(in_labels_dir, json_file)
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        annotations = data.get("annotations", [])
        classes = {ann.get("class") for ann in annotations}
        valid = classes & target_classes    # 타겟 클래스 포함 여부 확인
        if not valid:
            continue

        filename = data.get("image", {}).get("filename", "")
        img_path = os.path.join(in_images_dir, filename)
        if not filename or not os.path.exists(img_path):
            continue

        # 각 클래스별로 카운트 증가
        for cls in valid:
            if cls in target_classes:
                candidate_class_counts[cls] += 1
        
        # 후보로 추가
        candidates.append((json_file, filename, valid))

    # 후보 탐색 결과 출력
    print(f"✅ 전체 후보 수: {len(candidates)}장")
    for cls in sorted(target_classes):
        print(f"   └ 클래스 {cls}: {candidate_class_counts[cls]}장")

    random.seed(42)             # 시드 고정
    random.shuffle(candidates)  # 랜덤 셔플로 다양성 확보

    # 병렬로 필터링하는 함수
    def parallel_filter(threshold):
        args_list = [(json_file, filename, class_set, threshold, in_images_dir)
                     for json_file, filename, class_set in candidates]

        # 병렬 처리(최대 8개 스레드 사용)
        with ThreadPoolExecutor(max_workers=8) as executor:
            for future in as_completed([executor.submit(evaluate_candidate, args) for args in args_list]):
                result = future.result()
                if not result:
                    continue
                json_file, filename, class_set = result

                # 클래스별로 quota 초과하지 않는 선에서 추가
                for cls in class_set:
                    if cls in target_classes and len(selected[cls]) < per_class_target:
                        selected[cls].add((json_file, filename))
                        break

    # 1차 필터: 엄격 기준
    parallel_filter(QUALITY_THRESHOLD)

    # 2차 필터: 부족한 클래스에 한해 완화 기준 적용
    for cls in target_classes:
        if len(selected[cls]) < per_class_target:
            print(f"⚠️ 클래스 {cls}: {per_class_target - len(selected[cls])}장 부족 → 기준 완화")
            parallel_filter(RELAXED_THRESHOLD)

    # 최종 선택된 이미지 및 라벨 복사
    for cls, files in selected.items():
        count = min(len(files), per_class_target)
        for json_file, fname in list(files)[:count]:
            # 클래스별 하위 디렉토리 생성
            img_class_dir = os.path.join(out_images_dir, cls)
            json_class_dir = os.path.join(out_labels_dir, cls)

            src_img = os.path.join(in_images_dir, fname)
            dst_img = os.path.join(img_class_dir, fname)
            src_json = os.path.join(in_labels_dir, json_file)
            dst_json = os.path.join(json_class_dir, json_file)

            if not os.path.exists(src_img):
                continue

            os.makedirs(os.path.dirname(dst_img), exist_ok=True)
            os.makedirs(os.path.dirname(dst_json), exist_ok=True)

            shutil.copy(src_img, dst_img)
            shutil.copy(src_json, dst_json)
            class_counts[f"{split}_{cls}"] += 1

# 최종 결과 출력
print("\n📊 최종 결과")
for key in sorted(class_counts.keys()):
    print(f"{key}: {class_counts[key]}장")
print(f"총 저장 이미지 수: {sum(class_counts.values())}장")