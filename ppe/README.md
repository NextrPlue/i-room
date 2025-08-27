# PPE (Personal Protective Equipment) Service

> i-room 프로젝트의 AI 기반 실시간 개인보호장비 착용 감지 및 모니터링 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [🏗️ 아키텍처](#아키텍처)
> - [📋 주요 기능](#주요-기능)
> - [🤖 AI 모델 및 알고리즘](#ai-모델-및-알고리즘)
> - [🌐 환경별 설정](#환경별-설정)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>

## 📄 서비스 소개

i-room 프로젝트의 AI 기반 실시간 개인보호장비(PPE) 착용 감지 및 모니터링 마이크로서비스입니다. WebRTC를 활용한 실시간 영상 스트리밍과 YOLOv8+BoT-SORT 기반 객체 탐지/추적 기술을 통해
안전모, 안전고리 착용 여부를 실시간으로 감시하고 위반 사항을 즉시 알림합니다.

### 핵심 기능

- **실시간 영상 스트리밍**: WebRTC 기반 실시간 CCTV 영상 송수신 및 모니터링
- **AI 기반 PPE 탐지**: YOLOv8 모델을 활용한 안전모/안전벨트 착용 여부 실시간 탐지
- **다중 객체 추적**: BoT-SORT 알고리즘을 통한 보호구 추적 및 고유 track ID 관리
- **지능형 위반 감지**: 오탐 방지를 위한 홀드 시스템 및 동적 임계값 조정
- **실시간 알림 시스템**: 위반 사항 감지 시 즉시 알림 발송 및 위반 이력 관리

<a id="개발자"></a>

## 🧑‍💻 개발자

|             | 장준혁                                                       | 김민수                                                    |
|-------------|-----------------------------------------------------------|--------------------------------------------------------|
| **E-Mail**  | kalina01255@naver.com                                     | minsue9608@naver.com                                   |
| **GitHub**  | [angrynison](https://github.com/angrynison)               | [K-Minsu](https://github.com/K-Minsu)                  |
| **Profile** | <img src="https://github.com/angrynison.png" width=100px> | <img src="https://github.com/K-Minsu.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 실시간 AI 비전 시스템 구축

> YOLOv8 + BoT-SORT 기반 실시간 객체 탐지/추적 시스템과 WebRTC 영상 스트리밍을 결합한 통합 솔루션을 구축했습니다. 오탐 방지를 위한 홀드 시스템, 동적 임계값 조정, ID 병합 로직 등을
적용하여 실제 현장에서 활용 가능한 신뢰성 높은 PPE 모니터링 시스템을 제공합니다.

<a id="시작-가이드"></a>

## 🚀 시작 가이드

### 사전 준비 사항

- Python 3.9 이상
- pip
- 출근한 근로자가 있는지 확인(watch DB의 데이터 유무 확인)
### 서비스 실행

1. **가상환경 생성/활성화**
   ```bash
   <python_path> -m venv safety_env
   source safety_env/Scripts/activate
   ```
   
2. **프로젝트 클론 및 디렉토리 이동**
   ```bash
   git clone {저장소 URL}
   cd i-room/ppe
   ```

3. **필요 라이브러리 설치**
   ```bash
   pip install -r requirements.txt
   ```

4. **환경 변수 설정**
   ```bash
   # .env 파일 생성 및 설정
   MODEL_PATH=ppe/model/best.pt       # YOLOv8 모델 파일 경로
   RTSP_URL=ppe/<Web RTC의 중계서버 URL>   # 비디오 소스 (RTSP URL 또는 파일)
   STUN_URLS=stun:stun.l.google.com:19302
   ICE_FORCE_RELAY=false
   ```

5. **FastAPI 서버 실행 - 최상위 폴더에서 실행**
   ```bash
   uvicorn ppe.main:app --host 127.0.0.1 --port 8000
   ```

6. **ngrok로 퍼블릭 도메인 받기(별도 터미널에서 실행)**
   ```bash
   ngrok http 8000
   # 예시 도메인 : https://fcff8db546c8.ngrok-free.app/monitor
   ```
<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Python 3.9+**: 프로그래밍 언어
- **FastAPI**: 고성능 웹 프레임워크
- **Uvicorn**: ASGI 서버
- **WebRTC (aiortc)**: 실시간 영상 스트리밍
- **YOLOv8 (Ultralytics)**: 객체 탐지 모델
- **BoT-SORT**: 다중 객체 추적 알고리즘
- **PyTorch**: 딥러닝 프레임워크 (CUDA 지원)
- **OpenCV**: 이미지/영상 처리 라이브러리
- **SQLAlchemy**: ORM (데이터베이스 연동)
- **PyMySQL**: MySQL 데이터베이스 연결
- **Deep Sort Realtime**: 실시간 객체 추적
- **AWS**: TURN 서버 및 ICE 사용을 위한 인스턴스 사용

<a id="아키텍처"></a>

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Video Source  │    │   PPE Service   │    │    MySQL DB     │
│                 │    │                 │◄──►│   (violations)  │
│ - CCTV/RTSP     │    │ ┌─────────────┐ │    │                 │
│ - Video File    │    │ │ WebRTC      │ │    │ - incident_id   │
└─────────────────┘    │ │ Streaming   │ │    │ - worker_id     │
         │             │ └─────────────┘ │    │ - ppe_type      │
         ▼             │        │        │    │ - timestamp     │
┌─────────────────┐    │ ┌─────────────┐ │    └─────────────────┘
│  Browser Client │    │ │ AI Pipeline │ │
│                 │◄──►│ │             │ │
│ - Monitor UI    │    │ │ 1.YOLOv8    │ │    ┌─────────────────┐
│ - WebRTC View   │    │ │ 2.BoT-SORT  │ │    │ Alarm Service   │
└─────────────────┘    │ │ 3.PPE Check │ │◄──►│                 │
          ▲            │ └─────────────┘ │    │ - Violation     │
          └────────────┼────────│        │    │   Notification  │
                       │ ┌─────────────┐ │    └─────────────────┘
                       │ │ Detection   │ │
                       │ │ Control API │ │
                       │ └─────────────┘ │
                       └─────────────────┘
```

<a id="주요-기능"></a>

## 📋 주요 기능

### 1. 실시간 영상 스트리밍

- **WebRTC 기반 스트리밍**: `/monitor` 웹 인터페이스를 통한 실시간 영상 모니터링
- **TURN/STUN 서버 지원**: NAT 환경에서의 안정적인 WebRTC 연결
- **적응적 품질 조정**: 네트워크 상황에 따른 해상도/FPS 동적 조정

### 2. AI 기반 PPE 탐지

- **YOLOv8 모델**: 안전모/안전벨트 착용/미착용 4개 클래스 탐지
- **BoT-SORT 추적**: 다중 객체 실시간 추적 및 ID 관리
- **GPU 가속**: CUDA 지원을 통한 고속 추론 (CPU 폴백 지원)

### 3. 지능형 위반 감지 시스템 (위반 발생 판단)

- **홀드 시스템**: 순간적 오탐 방지를 위한 시간 기반 필터링
- **동적 임계값**: 근로자 수 기반 신뢰도 임계값 자동 조정
- **ID 병합 로직**: 추적 ID 변경 시에도 동일 인물 인식 유지
- **ON-근접 억제**: 착용 감지 직후 순간적인 미착용 오탐 방지

### 4. 위반 관리 및 알림

- **실시간 알림**: 위반 감지 시 즉시 알림 시스템 연동
- **중복 방지**: 동일 위반에 대한 반복 알림 억제
- **상태 추적**: 위반 상태의 시작/지속/해제 관리 (incident)

<a id="ai-모델-및-알고리즘"></a>

## 🤖 AI 모델 및 알고리즘

### YOLOv8 모델

- **모델 파일**: `ppe/model/best.pt`
- **탐지 클래스**:
    - `helmet_on`: 안전모 착용
    - `helmet_off`: 안전모 미착용
    - `harness_on`: 안전벨트 착용
    - `harness_off`: 안전벨트 미착용
- **신뢰도 임계값**: 동적 조정 

### BoT-SORT 추적 알고리즘

- **설정 파일**: `ppe/my_botsort.yaml`
- **추적 기능**: 다중 객체 실시간 추적 및 ID 일관성 유지
- **ID 병합**: 거리 기반 동일 인물 판별 및 추적 ID 통합

### 위반 감지 로직 파라미터

```python
# 핵심 파라미터
OFF_HOLD_S = 5.0          # 홀드 시간 (초)
OFF_HOLD_MIN_HIT = 20     # 최소 감지 횟수
ON_GRACE_S = 3.0          # 착용 후 억제 시간
STRONG_ON_CONF = 0.65     # 억제를 위한 착용 신뢰도
EXPIRE_S = 30             # 위반 상태 만료 시간
```

<a id="환경별-설정"></a>

## 🌐 환경별 설정

### 로컬 개발 환경

- **비디오 소스**: 로컬 파일 (`ppe/helmet_off.mp4`)
- **WebRTC**: STUN 서버 사용
- **데이터베이스**: SQLAlchemy 기본 설정

### 프로덕션 환경

- **비디오 소스**: RTSP 스트림 URL
- **WebRTC**: AWS의 EC2를 통해 TURN 서버 포함 ICE 설정
- **GPU**: CUDA 가속 활성화

### Docker 환경

- **모델 마운트**: 학습된 모델 파일 볼륨 마운트
- **네트워크**: WebRTC 포트 설정 및 방화벽 구성

<a id="api-명세"></a>

## 📡 API 명세

### WebRTC 모니터링

- **모니터 페이지**: `GET /monitor` - 실시간 영상 모니터링 인터페이스
- **TURN 자격증명**: `GET /turn-cred` - WebRTC TURN 서버 자격증명
- **SDP 교환**: `POST /offer` - WebRTC Offer/Answer 처리

### 탐지 제어

- **탐지 시작**: `POST /detect/start` - AI 탐지 프로세스 시작
- **탐지 중지**: `POST /detect/stop` - AI 탐지 프로세스 중지
- **상태 확인**: `GET /detect/status` - 탐지 프로세스 상태 조회

### GPS 및 기타

- **GPS 라우터**: `/gps/**` - 위치 정보 관련 API

PPE 서비스의 상세 API 명세는 아래 Notion 링크의 'PPE' 섹션을 참고하십시오.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)