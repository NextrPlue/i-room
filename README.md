# i-room

| 지능형 건설현장 근로자 안전 통합관리 플랫폼 "이룸(I-Room)"
> 목차
> - [📌 프로젝트 소개](#프로젝트-소개)
> - [👩‍👩‍👧‍👧 팀원 소개](#팀원-소개)
> - [✏️ 주요 기능](#주요-기능)
> - [🔗 링크 모음](#링크-모음)
> - [📡 API 명세](#api-명세)
> - [🚩 시작 가이드](#시작-가이드)
> - [🖥️ 기술 스택](#기술-스택)
> - [📁 프로젝트 구조](#프로젝트-구조)

<br>

## 프로젝트 소개

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

<br>

## 팀원 소개

|        | 이성훈                                                      | 김민수                                                    | 박소연                                                     | 배소정                                                   | 장준혁                                                       | 조승빈                                                         |
|--------|----------------------------------------------------------|--------------------------------------------------------|---------------------------------------------------------|-------------------------------------------------------|-----------------------------------------------------------|-------------------------------------------------------------|
| 역할     | Backend (Spring), Frontend                               | AI (FastAPI)                                           | Backend (Spring), Frontend                              | Backend (Spring), Frontend                            | AI (FastAPI)                                              | Backend (Spring)                                            |
| E-Mail | p.plue1881@gmail.com                                     | minsue9608@naver.com                                   | gumza9go@gmail.com                                      | bsj9278@gmail.com                                     | kalina01255@naver.com                                     | benscience@naver.com                                        |
| GitHub | https://github.com/NextrPlue                             | https://github.com/K-Minsu                             | https://github.com/sorasol9                             | https://github.com/BaeSJ1                             | https://github.com/angrynison                             | https://github.com/changeme4585                             |
|        | <img src="https://github.com/NextrPlue.png" width=100px> | <img src="https://github.com/K-Minsu.png" width=100px> | <img src="https://github.com/sorasol9.png" width=100px> | <img src="https://github.com/BaeSJ1.png" width=100px> | <img src="https://github.com/angrynison.png" width=100px> | <img src="https://github.com/changeme4585.png" width=100px> |

<br>

## 주요 기능

|       구분        |        기능         | 설명                                                                 |         담당 서비스          |
|:---------------:|:-----------------:|:-------------------------------------------------------------------|:-----------------------:|
| **실시간 안전 모니터링** |     **대시보드**      | 현장 전체 상황, 근로자 상태, 위험 알림 등을 실시간으로 시각화                               | `Dashboard`, `Frontend` |
|                 |    **실시간 알림**     | 위험 감지 시 관리자 페이지와 근로자 페이지에 SSE를 통해 실시간 알림 전송                        |   `Alarm`, `Frontend`   |
|                 | **IoT 센서 데이터 처리** | 현장 IoT 장비로부터 TCP 소켓 통신으로 센서 데이터를 수신하여 Kafka로 전송                    |        `Sensor`         |
| **AI 기반 위험 예측** |  **안전장비 착용 감지**   | CCTV 영상 프레임을 분석하여 안전모, 안전고리 등 개인보호장비(PPE) 착용 여부를 AI 모델(YOLOv8)로 탐지 |          `PPE`          |
|                 |   **건강 위험도 예측**   | 센서 데이터(온도, 습도 등)를 기반으로 AI 모델을 사용하여 온열질환 등 건강 위험도를 예측               |        `Health`         |
|    **통합 관리**    |  **사용자 및 권한 관리**  | 관리자 및 근로자 계정 정보, 역할 기반의 접근 제어(RBAC) 기능 제공                          |         `User`          |
|                 |   **현장 정보 관리**    | 근로자, 장비, 도면, 안전 가이드라인 등 현장 정보 통합 관리                                |      `Management`       |
|  **API 게이트웨이**  |   **인증 및 라우팅**    | 모든 마이크로서비스 요청에 대한 단일 진입점(entrypoint) 역할. JWT 기반 인증 및 동적 라우팅 처리     |        `Gateway`        |

<br>

## Architecture

<img width="1352" alt="image" src="https://github.com/user-attachments/assets/15168853-1b93-4b99-9e1a-a58146591843">

## 프로젝트 구조

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

<br>

## 링크 모음

| 기획 |                                                          디자인                                                          | 개발 |
|:--:|:---------------------------------------------------------------------------------------------------------------------:|:--:|
|    | [와이어프레임](https://www.figma.com/design/YoPKzEsC8NfWVUHoTaZPi0/%EC%9D%B4%EB%A3%B8?node-id=0-1&p=f&t=qhCswsEEfx0Y42VE-0) |    |

<br>

## API 명세

> [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)

<br>

## 시작 가이드

### 사전 준비 사항

서비스를 실행하기 전에 다음 도구들이 설치되어 있는지 확인하십시오.

* Docker 및 Docker Compose
* JDK 17, Gradle 8.x (Spring Boot 서비스 빌드 시)
* Node.js, npm (Frontend 빌드 시)
* Python 3.9+, pip (FastAPI 서비스 실행 시)

### Docker Compose로 모든 서비스 실행하기

모든 마이크로서비스(백엔드, 게이트웨이, 프론트엔드) 및 Kafka를 시작하려면, 프로젝트 루트 디렉토리에서 다음 명령어를 실행합니다.

```bash
docker-compose -f build-docker-compose.yml up --build
```

애플리케이션 접속은 `http://localhost:8088` 에서 가능합니다.

### 개별 서비스 실행하기 (개발용)

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

### 유틸리티

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

<br>

## 기술 스택

|         구분         | 기술                                                                                                   | 설명                               |
|:------------------:|:-----------------------------------------------------------------------------------------------------|:---------------------------------|
|    **Backend**     | `Java 17`, `Spring Boot 3.x`, `Spring Cloud Gateway`, `Spring Data JPA`, `Spring Security`, `Gradle` | 안정적이고 확장 가능한 백엔드 시스템 구축          |
|                    | `Python 3.9`, `FastAPI`, `Uvicorn`                                                                   | AI 모델 서빙 및 빠른 API 개발             |
|    **Frontend**    | `React`, `JavaScript (ES6+)`, `React Query`, `Recoil`, `Axios`                                       | 컴포넌트 기반의 반응형 UI 및 효율적인 상태 관리     |
|       **AI**       | `YOLOv8`, `PyTorch`, `Scikit-learn`, `OpenCV`                                                        | CCTV 영상 분석 및 센서 데이터 기반 위험도 예측 모델 |
|    **Database**    | `MySQL`, `Qdrant`                                                                                    | RDBMS와 벡터 데이터베이스를 용도에 맞게 사용      |
| **DevOps & Infra** | `Docker`, `Docker Compose`, `Nginx`                                                                  | 컨테이너 기반의 서비스 환경 구축 및 배포          |
|                    | `Kafka`                                                                                              | 서비스 간 비동기 통신 및 이벤트 스트리밍          |
|                    | `Jenkins`, `Kubernetes (AKS)`                                                                        | CI/CD 파이프라인 및 클라우드 환경 배포 자동화     |

<br>

## 시연 영상
