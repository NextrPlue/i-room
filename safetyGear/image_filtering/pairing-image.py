import os
import shutil

## 남은 json 중 이미지와 매칭되지 않는 것 삭제

# ✅ 입력 및 출력 루트 설정
base_dir = r"C:\Users\kalin\Desktop"
input_root = os.path.join(base_dir, "completed")
output_root = os.path.join(base_dir, "perfect")

# ✅ 처리 대상
splits = ["train", "val"]
classes = ["01", "03", "07"]

for split in splits:
    for cls in classes:
        print(f"\n🔍 처리 중: {split}/{cls}")

        # 경로 설정
        img_dir = os.path.join(input_root, split, "images", cls)
        json_dir = os.path.join(input_root, split, "labels_json", cls)

        # 출력 경로 설정
        out_img_dir = os.path.join(output_root, split, "images", cls)
        out_json_dir = os.path.join(output_root, split, "labels_json", cls)
        os.makedirs(out_img_dir, exist_ok=True)
        os.makedirs(out_json_dir, exist_ok=True)

        # 이미지 기준으로 매칭 확인
        matched = 0
        removed_img = 0
        removed_json = 0

        if not os.path.exists(img_dir) or not os.path.exists(json_dir):
            print(f"❗ 존재하지 않는 경로: {img_dir} 또는 {json_dir}")
            continue

        img_files = [f for f in os.listdir(img_dir) if f.lower().endswith((".jpg", ".jpeg", ".png"))]
        json_files = set(os.listdir(json_dir))

        for img_file in img_files:
            base_name, _ = os.path.splitext(img_file)
            json_name = base_name + ".json"

            img_path = os.path.join(img_dir, img_file)
            json_path = os.path.join(json_dir, json_name)

            if json_name in json_files:
                # 복사
                shutil.copy(img_path, os.path.join(out_img_dir, img_file))
                shutil.copy(json_path, os.path.join(out_json_dir, json_name))
                matched += 1
            else:
                os.remove(img_path)
                removed_img += 1
                print(f"🗑️ 삭제된 이미지 (매칭되는 JSON 없음): {img_file}")

        # 남은 json 중 이미지와 매칭되지 않는 것 삭제
        img_basenames = set(os.path.splitext(f)[0] for f in img_files)
        for json_file in json_files:
            json_base = os.path.splitext(json_file)[0]
            if json_base not in img_basenames:
                os.remove(os.path.join(json_dir, json_file))
                removed_json += 1
                print(f"🗑️ 삭제된 JSON (매칭되는 이미지 없음): {json_file}")

        print(f"✅ 복사 완료: {matched}쌍 | 🗑️ 이미지 삭제: {removed_img} | 🗑️ JSON 삭제: {removed_json}")
