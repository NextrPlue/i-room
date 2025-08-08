# yolo_service.py
import asyncio
import cv2, time, torch, os
from ultralytics import YOLO
from ppe.services.ppe_check import check_violation
from ppe.services.violation_handler import handle_violation
from ppe.services.webrtc_service import frame_queue

MODEL_PATH = os.getenv("MODEL_PATH", "ppe/model/best_8m_v4.pt")
VIDEO_SRC  = os.getenv("RTSP_URL", "ppe/test2.mp4")
device     = "cuda" if torch.cuda.is_available() else "cpu"
model      = YOLO(MODEL_PATH).to(device)

_runner_task: asyncio.Task | None = None
_stop_flag = False

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
            half=True,
            conf=0.4,
        )

    while True:
        prev_time = time.time()

        helmet_seen, seatbelt_seen = set(), set()
        total_frames = 0

        try:
            for r in gen():
                if _stop_flag:
                    print("[INFO] detection stop requested")
                    return

                frame = r.plot()
                if frame is None:
                    await asyncio.sleep(0)
                    continue
                total_frames += 1

                # 변경: 프레임마다 등장한 track_id를 집합에 기록
                for box in r.boxes:
                    cls_id = int(box.cls[0])
                    tid = int(box.id[0]) if box.id is not None else -1
                    if cls_id == 0:      # seatbelt_on
                        seatbelt_seen.add(tid)
                    elif cls_id == 1:    # helmet_on
                        helmet_seen.add(tid)

                if not frame_queue.full():
                    await frame_queue.put(frame)
                
                # 10초마다 "등장한 적이 있는" 객체 수로 카운트
                if time.time() - prev_time >= interval_sec and total_frames > 0:
                    # tid == -1 (ID 미할당) 처리: 그대로 두면 -1이 있으면 1개로 계산.
                    helmet_count   = len(helmet_seen)
                    seatbelt_count = len(seatbelt_seen)

                    check_result = check_violation(helmet_count, seatbelt_count)
                    print(f"[CHECK] {check_result}")

                    if check_result["violation"]:
                        handle_violation(helmet_count, seatbelt_count, frame, check_result)

                    # 다음 10초 구간을 위해 초기화
                    helmet_seen.clear()
                    seatbelt_seen.clear()
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
