import cv2
import torch
from ultralytics import YOLO

MODEL_PATH = 'best2.pt'
VIDEO_PATH = "test2.mp4"
OUTPUT_VIDEO_PATH = "output_bot_sort.mp4"

CLASS_NAMES = {
    0: "seatbelt_on",
    1: "helmet_on",
}

device = "cuda" if torch.cuda.is_available() else "cpu"
print(f"📌 Using device: {device}")
model = YOLO(MODEL_PATH).to(device)

# stream=True로 메모리 누수 방지
results = model.track(
    source=VIDEO_PATH,
    conf=0.2,
    iou=0.5,
    tracker="botsort.yaml",
    stream=True,
    device=device,
    persist=True
)

# 비디오 저장 준비 (640x640으로 고정)
cap = cv2.VideoCapture(VIDEO_PATH)
fps = cap.get(cv2.CAP_PROP_FPS)
video_writer = cv2.VideoWriter(
    OUTPUT_VIDEO_PATH,
    cv2.VideoWriter_fourcc(*'mp4v'),
    fps,
    (640, 640)
)

for r in results:
    frame = r.orig_img.copy()

    orig_h, orig_w = frame.shape[:2]
    target_w, target_h = 640, 640

    # 프레임 크기를 640x640으로 제한
    frame = cv2.resize(frame, (target_w, target_h))

    for box in r.boxes:
        # 원본 크기 좌표
        x1, y1, x2, y2 = map(float, box.xyxy[0])

        # 좌표를 리사이즈된 프레임에 맞게 스케일링
        scale_x = target_w / orig_w
        scale_y = target_h / orig_h
        x1 = int(x1 * scale_x)
        y1 = int(y1 * scale_y)
        x2 = int(x2 * scale_x)
        y2 = int(y2 * scale_y)

        cls_id = int(box.cls[0])
        conf = float(box.conf[0])
        label = f"{CLASS_NAMES.get(cls_id, str(cls_id))} {conf:.2f}"
        color = (0, 255, 0)

        cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
        cv2.putText(frame, label, (x1, y1 - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

    cv2.imshow("YOLOv8 + BoT-SORT (640x640)", frame)
    video_writer.write(frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
video_writer.release()
cv2.destroyAllWindows()
print(f"🎬 결과 저장: {OUTPUT_VIDEO_PATH}")
