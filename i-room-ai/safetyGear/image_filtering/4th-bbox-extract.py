import os
import shutil

# 기본 경로 설정
base_origin = 'C:/Users/kalin/Desktop/completed'
base_bbox = 'C:/Users/kalin/Desktop/bbox'
output_base = 'C:/Users/kalin/Desktop/bbox_completed'

# 세트와 클래스 목록
sets = ['train', 'val']
classes = ['01', '03', '07']

# 각 세트와 클래스별 작업 수행
for set_name in sets:
    for class_name in classes:
        # bbox 기준 이미지 경로
        bbox_img_dir = os.path.join(base_bbox, set_name, 'images', class_name)
        if not os.path.exists(bbox_img_dir):
            continue

        # bbox에 있는 이미지 리스트
        image_files = [
            f for f in os.listdir(bbox_img_dir)
            if f.lower().endswith(('.jpg', '.jpeg', '.png'))
        ]

        # 원본 이미지/라벨 경로
        origin_img_dir = os.path.join(base_origin, set_name, 'images', class_name)
        origin_lbl_dir = os.path.join(base_origin, set_name, 'labels_json', class_name)

        # 출력 폴더 경로
        output_img_dir = os.path.join(output_base, set_name, 'images', class_name)
        output_lbl_dir = os.path.join(output_base, set_name, 'labels_json', class_name)
        os.makedirs(output_img_dir, exist_ok=True)
        os.makedirs(output_lbl_dir, exist_ok=True)

        for img_file in image_files:
            # 이미지 복사
            src_img_path = os.path.join(origin_img_dir, img_file)
            dst_img_path = os.path.join(output_img_dir, img_file)
            if os.path.exists(src_img_path):
                shutil.copy2(src_img_path, dst_img_path)

            # JSON 라벨 복사
            json_file = os.path.splitext(img_file)[0] + '.json'
            src_json_path = os.path.join(origin_lbl_dir, json_file)
            dst_json_path = os.path.join(output_lbl_dir, json_file)
            if os.path.exists(src_json_path):
                shutil.copy2(src_json_path, dst_json_path)
