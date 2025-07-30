import cv2
import torch
from ultralytics import YOLO

MODEL_PATH = 'best_8n_finished.pt'
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
    conf=0.25,
    iou=0.5,
    tracker="botsort.yaml",
    stream=True,
    device=device,
    persist=True
)

# 비디오 저장 준비
cap = cv2.VideoCapture(VIDEO_PATH)
w, h = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)), int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
fps = cap.get(cv2.CAP_PROP_FPS)
video_writer = cv2.VideoWriter(OUTPUT_VIDEO_PATH, cv2.VideoWriter_fourcc(*'mp4v'), fps, (w, h))

for r in results:
    frame = r.orig_img.copy()
    for box in r.boxes:
        x1, y1, x2, y2 = map(int, box.xyxy[0])
        cls_id = int(box.cls[0])
        conf = float(box.conf[0])
        label = f"{CLASS_NAMES.get(cls_id, str(cls_id))} {conf:.2f}"
        color = (0, 255, 0)

        cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
        cv2.putText(frame, label, (x1, y1 - 10),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

    cv2.imshow("YOLOv8 + BoT-SORT", frame)
    video_writer.write(frame)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
video_writer.release()
cv2.destroyAllWindows()
print(f"🎬 결과 저장: {OUTPUT_VIDEO_PATH}")
