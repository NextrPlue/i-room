import os
import json
import cv2
import numpy as np

# 경로 설정
base_dir = 'C:/Users/kalin/Desktop/completed'
output_dir = 'C:/Users/kalin/Desktop/bbox'
splits = ['train', 'val']
locations = ['01', '03', '07']
target_classes = ['01', '03', '07']  # 시각화 대상 클래스

def draw_box_and_polygon_bbox(image_path, annotations):
    img = cv2.imread(image_path)
    if img is None:
        print(f"이미지 파일을 찾을 수 없습니다: {image_path}")
        return None

    for ann in annotations:
        cls = ann.get('class', None)
        if cls not in target_classes:
            continue  # 원하는 클래스만 시각화

        try:
            if 'box' in ann:
                x1, y1, x2, y2 = ann['box']
            elif 'polygon' in ann:
                points = np.array(ann['polygon'])
                x1, y1 = points.min(axis=0)
                x2, y2 = points.max(axis=0)
                x1, y1, x2, y2 = map(int, [x1, y1, x2, y2])
            else:
                continue  # box도 polygon도 없으면 건너뜀

            instance_id = ann.get('Instance_id', '')
            label = f"{cls}-{instance_id}"

            # bbox 그리기
            cv2.rectangle(img, (x1, y1), (x2, y2), (0, 255, 0), 2)
            cv2.putText(img, label, (x1, y1 - 5),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 1)
        except Exception as e:
            print(f"오류 발생 (annotation 처리 중): {e}")
            continue

    return img

# 전체 폴더 순회
for split in splits:
    for loc in locations:
        json_dir = os.path.join(base_dir, split, 'labels_json', loc)
        image_dir = os.path.join(base_dir, split, 'images', loc)
        save_dir = os.path.join(output_dir, split, 'images', loc)
        os.makedirs(save_dir, exist_ok=True)

        if not os.path.exists(json_dir):
            print(f"JSON 경로 없음: {json_dir}")
            continue

        for json_file in os.listdir(json_dir):
            if not json_file.endswith('.json'):
                continue

            json_path = os.path.join(json_dir, json_file)
            try:
                with open(json_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)

                image_filename = data['image']['filename']
                image_path = os.path.join(image_dir, image_filename)

                # bbox + polygon-bbox 시각화
                result_img = draw_box_and_polygon_bbox(image_path, data['annotations'])

                if result_img is not None:
                    save_path = os.path.join(save_dir, image_filename)
                    cv2.imwrite(save_path, result_img)
            except Exception as e:
                print(f"JSON 처리 중 오류 발생: {json_path}")
                print(e)
