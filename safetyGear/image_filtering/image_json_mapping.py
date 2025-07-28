import os
import shutil

"""
⚠️⚠️⚠️ 실행 전 주의사항 ⚠️⚠️⚠️
다음과 같은 폴더 구조로 정리되어 있어야함.
<실행 전>
completed/
├── train/
│   └── images/
│       └── 01/, 03/, 07/
└── val/
    └── images/
        └── 01/, 03/, 07/

코드 실행 후, 자동으로 폴더 정리 됨.
<실행 후>
completed/
├── train/
│   ├── images/
│   │   └── 01/, 03/, 07/
│   └── labels_json/
│       └── 01/, 03/, 07/
└── val/
    ├── images/
    │   └── 01/, 03/, 07/
    └── labels_json/
        └── 01/, 03/, 07/
"""

# 폴더 이름 설정
src = "D:/" + "6.상업시설_신사동_복합_시설_completed"
dst = "D:/" + "민수_completed"

# 📂 경로 설정
SRC_BASE = os.path.normpath(src)
DST_BASE = os.path.normpath(dst)
TARGET_CLASSES = {"01", "03", "07"}

def sync_labels_from_source():
    for split in ["train", "val"]:
        print(f"\n📂 {split.upper()} 세트 라벨 복사 시작")

        for cls in TARGET_CLASSES:
            img_dir = os.path.join(DST_BASE, split, "images", cls)
            src_label_dir = os.path.join(SRC_BASE, split, "labels_json", cls)
            dst_label_dir = os.path.join(DST_BASE, split, "labels_json", cls)

            if not os.path.exists(img_dir):
                print(f"❌ 이미지 폴더 없음: {img_dir}")
                continue

            os.makedirs(dst_label_dir, exist_ok=True)

            # 이미지 파일명 기준으로 JSON 복사
            for img_file in os.listdir(img_dir):
                if not img_file.lower().endswith((".jpg", ".jpeg", ".png")):
                    continue

                json_file = os.path.splitext(img_file)[0] + ".json"
                src_json_path = os.path.join(src_label_dir, json_file)
                dst_json_path = os.path.join(dst_label_dir, json_file)

                if os.path.exists(src_json_path):
                    shutil.copy(src_json_path, dst_json_path)
                else:
                    print(f"⚠️ 누락된 라벨: {json_file} (클래스 {cls})")

    print("\n✅ 라벨 복사 완료!")

# 🚀 실행
sync_labels_from_source()