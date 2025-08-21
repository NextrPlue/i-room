# yolo_service.py
import asyncio
import cv2, time, torch, os
from ultralytics import YOLO
from ppe.services.ppe_check import check_violation
from ppe.services.violation_handler import handle_violation
from ppe.services.webrtc_service import frame_queue
from dotenv import load_dotenv

load_dotenv()

MODEL_PATH = os.getenv("MODEL_PATH", "ppe/model/best.pt")
# VIDEO_SRC  = os.getenv("RTSP_URL", "ppe/test2.mp4")
VIDEO_SRC = os.getenv("RTSP_URL")
device     = "cuda" if torch.cuda.is_available() else "cpu"
model      = YOLO(MODEL_PATH).to(device)

_runner_task: asyncio.Task | None = None
_stop_flag = False

# 클래스 이름을 안전하게 얻는 유틸
def _name(model, cls_id: int) -> str:
    names = getattr(model, "names", None) or getattr(getattr(model, "model", None), "names", None)
    if isinstance(names, dict): return names.get(int(cls_id), str(cls_id))
    if isinstance(names, (list, tuple)):
        i = int(cls_id)
        return names[i] if 0 <= i < len(names) else str(cls_id)
    return str(cls_id)

# bbox 보조 유틸
def _xyxy(box):
    x1, y1, x2, y2 = map(float, box.xyxy[0])
    return (x1, y1, x2, y2)

def _uvb(box):
    x1, y1, x2, y2 = map(float, box.xyxy[0])
    return (int((x1 + x2) / 2), int(y2))

# 텍스트 라벨(배경 포함) 그리기: id는 절대 포함하지 않음
def _draw_label(img, x1: int, y1: int, text: str):
    (tw, th), bl = cv2.getTextSize(text, cv2.FONT_HERSHEY_SIMPLEX, 0.7, 2)
    ty1 = max(0, y1 - th - 6)
    ty2 = y1
    cv2.rectangle(img, (x1, ty1), (x1 + tw + 6, ty2), (255, 0, 0), -1)         # 배경
    cv2.putText(img, text, (x1 + 3, y1 - 4), cv2.FONT_HERSHEY_SIMPLEX, 0.7,
                (255, 255, 255), 2, cv2.LINE_AA)                               # 글자

async def run_detection_loop(interval_sec: int = 10, loop_file: bool = False):
    global _stop_flag
    _stop_flag = False

    print(f"[INFO] YOLO detection started → {VIDEO_SRC}")

    # Ultralytics의 stream 제너레이터를 비동기로 돌릴 수 있게 래핑 -> 서버 실행과 추적 시작을 분리
    def gen():
        return model.track(
            source=VIDEO_SRC,
            tracker="ppe/my_botsort.yaml",
            stream=True,
            device=device,
            persist=True,
            half=(device == "cuda"),
            conf=0.4,
            # vid_stride=2, 
        )

    while True:
        prev_time = time.time()

        # 10초 윈도우 이벤트 버퍼
        on_events, off_events = [], []
        total_frames = 0

        try:
            for r in gen():
                if _stop_flag:
                    print("[INFO] detection stop requested")
                    return

                # 1) 화면에는 '라벨 전체 비활성화'로 박스만 그림 (id가 절대 노출되지 않도록)
                frame = r.plot(labels=False)
                if frame is None:
                    await asyncio.sleep(0)
                    continue
                total_frames += 1

                # 2) 우리의 라벨(클래스 + conf)만 직접 그리기
                if r.boxes is not None and len(r.boxes) > 0:
                    for box in r.boxes:
                        # --- 화면용 라벨 ---
                        x1, y1, x2, y2 = map(int, box.xyxy[0])
                        cls_id = int(box.cls[0])
                        cname = _name(model, cls_id)
                        conf  = float(box.conf[0]) if hasattr(box, "conf") else 0.0
                        _draw_label(frame, x1, y1, f"{cname} {conf:.2f}")  

                        # --- 이벤트(on/off) 수집용 ---
                        tid = int(box.id[0]) if box.id is not None else -1
                        if tid == -1:
                            # tid == -1 (ID 미할당) → 이벤트/집계 제외
                            continue

                        ev = {
                            "t": time.time(),
                            "name": cname,   # 학습 names: [harness_on, harness_off, helmet_on, helmet_off]
                            "conf": conf,
                            "xyxy": _xyxy(box),
                            "uvb": _uvb(box),
                            "tid": tid,
                        }

                        if cname in ("harness_on", "helmet_on"):
                            on_events.append(ev)
                        elif cname in ("harness_off", "helmet_off"):
                            off_events.append(ev)
                        # 다른 클래스는 무시

                # 3) 프레임 송출
                if not frame_queue.full():
                    await frame_queue.put(frame)

                # 4) 10초마다 검사기 호출
                if time.time() - prev_time >= interval_sec and total_frames > 0:
                    result = check_violation(on_events, off_events)
                    print(f"[CHECK] {result}")

                    if result.get("violation"):
                        handle_violation(frame, result)  # result = {"violation": bool, "reason": str}

                    # 다음 10초 구간 초기화
                    on_events.clear()
                    off_events.clear()
                    total_frames = 0
                    prev_time = time.time()

                await asyncio.sleep(0)

        except Exception as e:
            print("[ERROR] detection loop:", e)

        # 파일 소스면 끝나고 나서 반복 여부
        if os.path.isfile(VIDEO_SRC) and not loop_file:
            print("[INFO] file source finished")
            return
        await asyncio.sleep(0.3)  # 재시도 텀

async def start_detection(loop_file: bool = False) -> bool:
    """이미 돌고 있지 않으면 백그라운드 태스크 시작"""
    global _runner_task
    if _runner_task and not _runner_task.done():
        return False
    _runner_task = asyncio.create_task(run_detection_loop(loop_file=loop_file))
    return True

def stop_detection() -> bool:
    """루프 정지 플래그만 올림"""
    global _stop_flag
    _stop_flag = True
    return True
