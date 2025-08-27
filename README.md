# i-room

> 지능형 건설현장 근로자 안전 통합관리 플랫폼 "이룸(I-Room)"

> ## 📋 목차
> - [⚡ TL;DR](#tldr)
> - [📌 프로젝트 소개](#프로젝트-소개)
> - [👩‍👩‍👧‍👧 팀원 소개](#팀원-소개)
> - [🏗️ 서비스 아키텍처 개요](#서비스-아키텍처-개요)
> - [✏️ 주요 기능](#주요-기능)
> - [🖼️ 아키텍처](#아키텍처)
> - [📁 프로젝트 구조](#프로젝트-구조)
> - [🚩 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [☁️ 배포 아키텍처](#배포-아키텍처)
> - [📡 API 명세서](#명세서)
> - [🎬 시연 영상](#시연-영상)

---

<a id="tldr"></a>

## ⚡ TL;DR

> **이룸(i-Room)**: 건설현장 안전사고를 예방하는 AI 기반 통합 안전관리 플랫폼

### 🚀 핵심 특징

- **8개 마이크로서비스** + **2개 웹앱**으로 구성된 MSA 아키텍처
- **실시간 AI 분석**: YOLOv8 PPE 탐지 + XGBoost 건강 위험 예측
- **실시간 통신**: WebSocket/STOMP + Kafka 이벤트 스트리밍

### 📊 기대 성과

- **조치 리드타임 90% 단축** (20분 → 2분)
- **중대재해 발생률 35% 감소**
- **보호구 미착용 감지 95% 정확도**

### 🛠️ 주요 기술

- **Backend**: Spring Boot 3.5.3, FastAPI, MySQL 8.0, Apache Kafka
- **Frontend**: React 19.1, WebSocket/STOMP
- **AI**: YOLOv8, BoT-SORT, XGBoost, LightGBM, WebRTC
- **Infra**: Azure Kubernetes Service (AKS), Docker, Jenkins

### 🏃‍♂️ 빠른 시작

```bash
# 전체 서비스 실행
docker-compose -f build-docker-compose.yml up --build

# PPE 서비스 (별도 실행 필요)
cd ppe/ && python -m venv venv && source venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8002 --reload
```

---

<a id="프로젝트-소개"></a>

## 📌 프로젝트 소개

### 개발 동기 및 목적

대한민국의 건설업 사고사망만인율은 OECD 경제 10대국 평균의 약 2.0배에 달하며[한국건설산업연구원, 2025.08.14], 국내 전체 산업재해 사망자의 46.8%가 건설 현장에서 발생하고
있습니다[고용노동부, 2025.03.11]. 이는 단순 사고가 아닌, 건설공사 관리감독·안전관리 부실로 인한 구조적인 문제입니다[동아일보, 2022.12.12]. 저희는 이 데이터를 통해 증명되는 심각한 현실을
바꾸고자 했습니다.

현장의 진짜 문제는 '시스템의 분절'에 있습니다. 현장에서 복합적인 위험이 동시에 발생해도, 시스템이 나뉘어 있어 종합적인 상황 파악이 어렵습니다. 기존의 해법들 역시 CCTV 영상이라는 단일 센서에 의존해 오탐이
잦고, 사후 보고 중심이라 실시간 대응이 거의 불가능합니다.

그래서 저희는 **스마트 기술로 선제적 현장 안전사고 예방 및, 빠른 사고 대처와 통제 체계를 구축**하고자 합니다.

<br>

### 서비스 소개

> "이룸(i-Room)"은 지능형 건설현장 근로자 안전 통합관리 플랫폼입니다.

저희 플랫폼은 **'통합 모니터링', '보호구 착용 검사', '근로자 건강 이상 분석', 그리고 '안전 보고서 생성'** 이라는 네 가지 핵심 기능으로 동작합니다.

이를 통해 저희가 달성하고자 하는 핵심 목표는 세 가지입니다:

- **예방 중심의 통합 시스템**: 사전 예방과 실시간 대응을 동시에 제공
- **멀티모달 데이터 융합**: 통합 분석을 통한 정확한 위험 감지
- **개별 맞춤형 안전 관리**: 개별 근로자의 상태를 분석하여 위험을 예측하고, 맞춤형 알림 제공

**결론적으로, 사전 예방 중심으로 건설현장 안전관리 방식을 전환하는 것이 저희의 목표입니다.**

<br>

### 기대 효과

#### 📊 안전 지표 개선

- **조치 리드타임 90% 단축**: 위험 발생 시 기존 20분 → 2분으로 대응 시간 단축
- **중대재해 발생률 35% 감소**: 사전 예방을 통한 현장 안전성 향상
- **보호구 미착용 감지 정확도 95% 이상**: 관리자 직접 단속 대비 감지 누락률 70% 이상 감소

#### ⚡ 운영 효율성 향상

- **보고서 작성 시간 83% 단축**: 수작업 1시간 → 자동화 5분
- **순찰·점검 시간 80% 이상 절감**: 실시간 모니터링을 통한 효율적 현장 관리

#### 💰 비용 절감 효과

- **직접 비용 절감**: 안전사고 보상금, 벌금, 작업 중단 비용 감소
- **간접 비용 절감**: 기업 이미지 보호, 보험료 인하 등 부가적 경제 효과

<br>

### 개발 기간

2025.07.18 ~ 2025.09.01

---

<a id="팀원-소개"></a>

## 👩‍👩‍👧‍👧 팀원 소개

|        | 이성훈                                                      | 김민수                                                    | 박소연                                                     | 배소정                                                   | 장준혁                                                       | 조승빈                                                         |
|--------|----------------------------------------------------------|--------------------------------------------------------|---------------------------------------------------------|-------------------------------------------------------|-----------------------------------------------------------|-------------------------------------------------------------|
| 역할     | Backend, Frontend, AI, Cloud                             | Backend, Data, AI, Network                             | Backend, Frontend, AI, Wear OS                          | Backend, Frontend, AI                                 | Backend, Data, AI                                         | Backend, Data, AI                                           |
| E-Mail | p.plue1881@gmail.com                                     | minsue9608@naver.com                                   | gumza9go@gmail.com                                      | bsj9278@gmail.com                                     | kalina01255@naver.com                                     | benscience@naver.com                                        |
| GitHub | [NextrPlue](https://github.com/NextrPlue)                | [K-Minsu](https://github.com/K-Minsu)                  | [sorasol9](https://github.com/sorasol9)                 | [BaeSJ1](https://github.com/BaeSJ1)                   | [angrynison](https://github.com/angrynison)               | [changeme4585](https://github.com/changeme4585)             |
|        | <img src="https://github.com/NextrPlue.png" width=100px> | <img src="https://github.com/K-Minsu.png" width=100px> | <img src="https://github.com/sorasol9.png" width=100px> | <img src="https://github.com/BaeSJ1.png" width=100px> | <img src="https://github.com/angrynison.png" width=100px> | <img src="https://github.com/changeme4585.png" width=100px> |

---

<a id="서비스-아키텍처-개요"></a>

## 🏗️ 서비스 아키텍처 개요

i-room은 **8개의 마이크로서비스**와 **2개의 이중 웹 애플리케이션**으로 구성된 대규모 마이크로서비스 아키텍처입니다.

### 마이크로서비스 구성

| 서비스            | 기술 스택            | 주요 기능                               | 배포 상태 |
|----------------|------------------|-------------------------------------|-------|
| **Gateway**    | Spring Boot 3.x  | JWT 인증 및 API 라우팅                    | ✅ AKS |
| **User**       | Spring Boot 3.x  | 3가지 사용자 유형 관리 (Admin/Worker/System) | ✅ AKS |
| **Management** | Spring Boot 3.x  | 근로자 출입 관리 및 안전 교육                   | ✅ AKS |
| **Alarm**      | Spring Boot 3.x  | 실시간 WebSocket 알림                    | ✅ AKS |
| **Sensor**     | Spring Boot 3.x  | IoT 센서 데이터 처리 및 WebSocket           | ✅ AKS |
| **Dashboard**  | Spring Boot 3.x  | BFF패턴 대시보드 + AI 문서 처리               | ✅ AKS |
| **PPE**        | FastAPI + Python | YOLOv8 + WebRTC 기반 PPE 탐지           | 🟡 로컬 |
| **Health**     | FastAPI + Python | XGBoost/LightGBM 기반 건강 위험 예측        | ✅ AKS |

<br>

### 웹 애플리케이션

| 애플리케이션    | 사용자 | 배포 주소     | 주요 기능                        |
|-----------|-----|-----------|------------------------------|
| **관리자 앱** | 관리자 | `/admin`  | 대시보드, 모니터링, 근로자 관리, AI 문서 처리 |
| **근로자 앱** | 근로자 | `/worker` | 실시간 안전 알림, 개인 안전 정보, 모바일 최적화 |

---

<a id="주요-기능"></a>

## ✏️ 주요 기능

|       구분        |        기능         | 설명                                                  |         담당 서비스          |
|:---------------:|:-----------------:|:----------------------------------------------------|:-----------------------:|
| **실시간 안전 모니터링** |     **대시보드**      | 현장 전체 상황, 근로자 상태, 위험 알림 등을 실시간으로 시각화                | `Dashboard`, `Frontend` |
|                 |    **실시간 알림**     | 위험 감지 시 관리자와 근로자에게 WebSocket을 통해 실시간 알림 전송          |   `Alarm`, `Frontend`   |
|                 | **IoT 센서 데이터 처리** | 웨어러블 기기로부터 바이너리 데이터 수신 및 Kafka 이벤트 스트리밍             |        `Sensor`         |
| **AI 기반 위험 예측** |   **PPE 착용 감지**   | YOLOv8+BoT-SORT 기반 실시간 안전모/안전고리 착용 감지 및 WebRTC 스트리밍 |          `PPE`          |
|                 |   **건강 위험도 예측**   | XGBoost/LightGBM 하이브리드 모델 + 규칙 엔진으로 온열질환 위험 예측      |        `Health`         |
|    **통합 관리**    |  **사용자 및 권한 관리**  | 3종류 사용자(Admin/Worker/System) 및 RBAC 기반 접근 제어        |         `User`          |
|                 |   **현장 정보 관리**    | 근로자 출입 관리, 장비 추적, 안전 교육 이력 관리                       |      `Management`       |
|  **API 게이트웨이**  |   **인증 및 라우팅**    | 단일 진입점 Spring Cloud Gateway + JWT 인증 및 동적 라우팅 처리    |        `Gateway`        |

---

<a id="아키텍처"></a>

## 🖼️ 아키텍처

<img width="6603" height="3239" alt="아키텍처" src="https://github.com/user-attachments/assets/2f2c6fc3-ea6b-47eb-a885-5b5f159044d6" />

---

<a id="프로젝트-구조"></a>

## 📁 프로젝트 구조

```
i-room/
├── alarm/         # (Spring) 실시간 알림 서비스
├── dashboard/     # (Spring) 대시보드 데이터 제공 서비스 (BFF)
├── frontend/      # (React) UI (관리자/근로자 페이지)
├── gateway/       # (Spring) API 게이트웨이
├── health/        # (FastAPI) 건강 위험도 예측 AI 서비스
├── management/    # (Spring) 현장 정보 관리 서비스
├── module-common/ # 공통 모듈
├── ppe/           # (FastAPI) 안전장비 착용 감지 AI 서비스
├── sensor/        # (Spring) IoT 센서 데이터 처리 서비스
└── user/          # (Spring) 사용자 관리 서비스
```

---

<a id="시작-가이드"></a>

## 🚩 시작 가이드

### 사전 준비 사항

서비스를 실행하기 전에 다음 도구들이 설치되어 있는지 확인하십시오.

* Docker 및 Docker Compose
* JDK 17, Gradle 8.x (Spring Boot 서비스 빌드 시)
* Node.js, npm (Frontend 빌드 시)
* Python 3.9+, pip (FastAPI 서비스 실행 시)

<br>

### 1. PPE 서비스 로컬 실행

```bash
# PPE 서비스 디렉토리로 이동
cd ppe/

# Python 가상환경 설정
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt

# 환경변수 설정 (선택사항)
export MODEL_PATH="ppe/model/best.pt"
export RTSP_URL="ppe/helmet_off.mp4"

# FastAPI 서버 실행
uvicorn main:app --host 0.0.0.0 --port 8002 --reload
```

<br>

### 2. 로컬 개발 환경 구성 (Docker Compose)

#### 전체 서비스 실행

```bash
# 모든 마이크로서비스 및 인프라 서비스 실행
docker-compose -f build-docker-compose.yml up --build

# 애플리케이션 접속: http://localhost:8088
```

#### 인프라만 실행 (개발용)

```bash
# 인프라 서비스 실행 (MySQL, Kafka, Qdrant)
docker-compose -f docker-compose.infra.yml up -d

# 전체 서비스 실행
docker-compose up -d

# 서비스 상태 확인
docker-compose ps
```

<br>

### 3. 개별 서비스 실행하기 (개발용)

각 마이크로서비스는 개별적으로 실행할 수 있습니다. 자세한 내용은 각 서비스 디렉토리의 `README.md` 파일을 참고하십시오.

* `gateway`: API 게이트웨이 (Spring)
* `user`: 사용자 관리 서비스 (Spring)
* `management`: 현장 정보 관리 서비스 (Spring)
* `dashboard`: 대시보드 데이터 제공 서비스 (Spring)
* `alarm`: 실시간 알림 서비스 (Spring)
* `sensor`: IoT 센서 데이터 처리 서비스 (Spring)
* `ppe`: 안전장비 착용 감지 서비스 (FastAPI)
* `health`: 건강 위험도 예측 서비스 (FastAPI)
* `frontend`: 관리자 및 근로자 UI (React)

<br>

### 4. 유틸리티

* **httpie** (curl / POSTMAN 대체 도구) 및 네트워크 유틸리티
  ```bash
  sudo apt-get update
  sudo apt-get install net-tools
  sudo apt install iputils-ping
  pip install httpie
  ```

* **kubernetes 유틸리티 (kubectl)**
  ```bash
  curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
  sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
  ```

---

<a id="기술-스택"></a>

## ⚙️ 기술 스택

|         구분         | 기술                                                                                                         | 설명                               |
|:------------------:|:-----------------------------------------------------------------------------------------------------------|:---------------------------------|
|    **Backend**     | `Java 17`, `Spring Boot 3.5.3`, `Spring Cloud Gateway`, `Spring Data JPA`, `Spring Security`, `Gradle 8.x` | 6개 마이크로서비스의 안정적이고 확장 가능한 백엔드 시스템 |
|                    | `Python 3.9+`, `FastAPI`, `Uvicorn`, `PyTorch`, `Scikit-learn`                                             | AI 모델 서빙 및 고성능 비동기 API 개발        |
|    **Frontend**    | `React 19.1`, `JavaScript (ES6+)`, `React Router DOM 7`, `STOMP.js + SockJS`, `Axios`                      | 이중 애플리케이션 및 실시간 WebSocket 통신     |
|       **AI**       | `YOLOv8 + BoT-SORT`, `XGBoost + LightGBM`, `WebRTC`, `OpenCV`, `Ultralytics`                               | 실시간 객체 탐지/추적 및 하이브리드 AI 모델       |
|    **Database**    | `MySQL 8.0` (각 서비스별), `Qdrant Vector DB`                                                                   | 마이크로서비스별 독립 DB + AI 문서 벡터 저장     |
|   **Messaging**    | `Apache Kafka`, `WebSocket/STOMP`                                                                          | 비동기 이벤트 스트리밍 및 실시간 양방향 통신        |
| **DevOps & Infra** | `Docker + Docker Compose`, `Kubernetes (AKS)`, `Nginx`, `Jenkins`                                          | 컨테이너화 및 Azure Kubernetes 클러스터 배포 |
|    **External**    | `OpenAI API`, `TURN/STUN 서버`                                                                               | AI 챗봇 서비스 및 WebRTC 연결 지원         |

---

<a id="배포-아키텍처"></a>

## ☁️ 배포 아키텍처

> i-room 프로젝트는 **하이브리드 배포 아키텍처**를 채택하여 서비스별 특성에 맞는 최적의 환경에서 운영됩니다.

### Azure Kubernetes Service (AKS)

8개의 마이크로서비스와 2개의 웹 애플리케이션이 AKS 클러스터에 배포되어 있습니다:

**배포된 서비스**

- **Spring Boot 서비스**: Gateway, User, Management, Alarm, Sensor, Dashboard
- **FastAPI 서비스**: Health (건강 위험도 예측)
- **React 애플리케이션**: 관리자 앱, 근로자 앱

**인프라 구성**

- **컨테이너 오케스트레이션**: Kubernetes를 통한 자동 스케일링 및 로드 밸런싱
- **로드 밸런서**: Nginx Ingress Controller를 통한 트래픽 분산
- **데이터베이스**: MySQL 8.0 (각 마이크로서비스별 독립 DB)
- **메시징**: Apache Kafka를 통한 이벤트 스트리밍
- **벡터 DB**: Qdrant를 통한 AI 문서 임베딩 저장

<br>

### 로컬 환경 (PPE 서비스)

PPE 서비스는 특수한 요구사항으로 인해 로컬 환경에서 운영됩니다:

**배포 이유**

- **WebRTC 실시간 스트리밍**: 저지연 비디오 처리 요구
- **GPU 가속 처리**: YOLOv8 + BoT-SORT 모델의 실시간 추론
- **TURN/STUN 서버**: 네트워크 환경에 따른 직접 연결 필요

**기술 구성**

- **서빙 프레임워크**: FastAPI + Uvicorn
- **AI 프레임워크**: PyTorch, Ultralytics, OpenCV
- **비디오 스트리밍**: WebRTC, aiortc
- **모델 처리**: GPU 가속 (CUDA) 및 CPU 폴백

<br>

### 네트워크 아키텍처

```
[사용자] → [Nginx Ingress] → [AKS 클러스터]
                                ├── Spring Boot Services
                                ├── React Applications
                                └── Infrastructure Services
```

<br>

### CI/CD 파이프라인

**자동화된 배포 프로세스**

- **소스 관리**: GitHub 레포지토리
- **빌드 도구**: Jenkins (AKS 클러스터 내 배포)
- **컨테이너화**: Docker 이미지 빌드 및 레지스트리 푸시
- **배포 전략**: Kubernetes 롤링 업데이트
- **모니터링**: 서비스 상태 및 로그 모니터링

**배포 흐름**

1. 코드 커밋 → GitHub webhook 트리거
2. Jenkins 빌드 파이프라인 실행
3. Docker 이미지 빌드 및 푸시
4. AKS 클러스터에 롤링 업데이트 배포
5. 서비스 헬스체크 및 로그 모니터링

---

<a id="명세서"></a>

## 📡 API 명세서

[API 명세서](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)

<img width="2268" height="2875" alt="API 명세서" src="https://github.com/user-attachments/assets/85cd4d00-77e4-4789-96ca-6f614088064c" />

---

<a id="시연-영상"></a>

## 🎬 시연 영상
