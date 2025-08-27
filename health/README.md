# Health Service

> i-room 프로젝트의 AI 기반 근로자 건강 모니터링 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [🏗️ 아키텍처](#아키텍처)
> - [📋 주요 기능](#주요-기능)
> - [🤖 AI 모델](#ai-모델)
> - [🌐 환경별 설정](#환경별-설정)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>

## 📄 서비스 소개

i-room 서비스의 AI 기반 근로자 건강 이상 감지 및 예측을 담당하는 마이크로서비스입니다. 웨어러블 센서 데이터를 실시간으로 분석하여 온열질환 등 건강 위험 상황을 예측하고 알림을 발송합니다.

### 핵심 기능

- **실시간 건강 모니터링**: Kafka를 통한 센서 데이터 실시간 수집 및 분석
- **AI 기반 위험 예측**: XGBoost/LightGBM 모델을 활용한 온열질환 위험도 예측
- **지능형 규칙 엔진**: 오탐 방지를 위한 맥락 기반 후처리 규칙 적용
- **사고 이력 관리**: 건강 이상 사고 기록 및 추적 관리

<a id="개발자"></a>

## 🧑‍💻 개발자

|        | 김민수                                                    |
|--------|--------------------------------------------------------|
| E-Mail | minsue9608@naver.com                                   |
| GitHub | https://github.com/K-Minsu                             |
|        | <img src="https://github.com/K-Minsu.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 하이브리드 AI 시스템 구축

> XGBoost/LightGBM 기반 온열질환 예측 모델과 규칙 기반 후처리 엔진을 결합한 하이브리드 시스템을 구축했습니다. 머신러닝 모델의 높은 예측 성능과 도메인 지식 기반 규칙의 해석 가능성을 결합하여 오탐을
> 최소화하고 실용적인 건강 모니터링 시스템을 제공합니다.

<a id="시작-가이드"></a>

## 🚀 시작 가이드

### 사전 준비 사항

- Python 3.9 이상
- pip

### 서비스 실행

1. **프로젝트 클론 및 디렉토리 이동**
   ```bash
   git clone {저장소 URL}
   cd i-room/health
   ```

2. **필요 라이브러리 설치**
   ```bash
   pip install -r requirements.txt
   ```

3. **필수 서비스 설정**
    - **Kafka**: `localhost:9092`에서 실행 중이어야 함
    - **MySQL**: 데이터베이스 설정 (SQLAlchemy 자동 테이블 생성)

4. **FastAPI 서버 실행**
   ```bash
   uvicorn app:app --reload --host 0.0.0.0 --port 8001
   ```

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Python 3.9+**: 프로그래밍 언어
- **FastAPI**: 고성능 웹 프레임워크
- **Uvicorn**: ASGI 서버
- **SQLAlchemy**: ORM (데이터베이스 연동)
- **Pydantic**: 데이터 검증 및 시리얼화
- **kafka-python**: Apache Kafka 클라이언트
- **XGBoost**: 그래디언트 부스팅 모델
- **LightGBM**: 경량 그래디언트 부스팅 모델
- **Scikit-learn**: 머신러닝 라이브러리
- **Pandas & NumPy**: 데이터 처리 및 분석
- **PyMySQL**: MySQL 데이터베이스 연결

<a id="아키텍처"></a>

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Sensor Service │    │ Health Service  │    │    MySQL DB     │
│                 │    │                 │◄──►│   (incidents)   │
│ - Worker Data   │    │ ┌─────────────┐ │    │                 │
│ - Vital Signs   │    │ │  Kafka      │ │    │ - incident_id   │
└─────────────────┘    │ │  Consumer   │ │    │ - worker_id     │
          │            │ └─────────────┘ │    │ - risk_level    │
          ▼            │        │        │    │ - timestamp     │
┌─────────────────┐    │ ┌─────────────┐ │    └─────────────────┘
│   Kafka Broker  │◄──►│ │ AI Pipeline │ │
│                 │    │ │             │ │
│  Topic: iroom   │    │ │ 1.Data Prep │ │
│ - WorkerSensor  │    │ │ 2.XGB/LGB   │ │
│ - HealthData    │    │ │ 3.Rule Eng  │ │
└─────────────────┘    │ └─────────────┘ │
          ▲            │        │        │
          │            │ ┌─────────────┐ │
          └────────────┼─│  Kafka      │ │
                       │ │  Producer   │ │
                       │ └─────────────┘ │
                       └─────────────────┘
                                ▲
                                │
                                ▼
                    ┌────────────────────────┐
                    │     Other Services     │
                    └────────────────────────┘
```

<a id="주요-기능"></a>

## 📋 주요 기능

### 1. 실시간 데이터 처리

- **Kafka Consumer**: `iroom` 토픽에서 센서 데이터 실시간 수신
- **데이터 전처리**: 센서 데이터 정규화 및 특성 추출
- **이상값 필터링**: Sanity check를 통한 비정상 데이터 제거

### 2. AI 기반 건강 위험 예측

- **XGBoost 모델**: 기본 온열질환 예측 모델
- **LightGBM 모델**: 백업 모델 (XGBoost 실패 시)
- **앙상블 예측**: 두 모델의 결과를 통합하여 신뢰성 향상

### 3. 지능형 규칙 엔진

- **R1 저심박 억제**: 심박수가 정상 범위일 때 오탐 방지
- **R2 활동 중 정상화**: 운동/작업 중 정상적인 심박 증가 구분
- **R3 저활동 고심박 확인**: 휴식 중 비정상 심박 증가 감지

### 4. 사고 관리

- **사고 기록**: `POST /incident/{incident_id}` - 건강 이상 사고 저장
- **사고 조회**: `GET /incident/{incident_id}` - 특정 사고 정보 조회
- **알림 발송**: Kafka를 통한 실시간 위험 알림

<a id="ai-모델"></a>

## 🤖 AI 모델

### 모델 구성

#### 1. XGBoost 모델

- **파일**: `models/baseline_xgb.json`
- **메타데이터**: `models/baseline_meta.json`
- **특징**: 높은 예측 성능, JSON 형식으로 저장

#### 2. LightGBM 모델

- **파일**: `models/lgb_model_1.pkl`, `models/lgb_model_2.pkl`
- **특징**: 빠른 추론 속도, Pickle 형식으로 저장

### 학습 및 연구

- **연구 노트북**: `training/` 디렉토리
    - `health_monitoring_AI_1.ipynb`: 기본 모델 개발
    - `health_monitoring_AI_2.ipynb`: 고도화 모델 개발
    - `XGBoost_Baseline_modeling.ipynb`: XGBoost 베이스라인
    - `hyperparameter_tuning.ipynb`: 하이퍼파라미터 최적화
    - `model_performance_comparison.ipynb`: 모델 성능 비교

### 규칙 엔진 구성

```python
# 규칙 파라미터 예시
RuleConfig:
    hr_ratio_veto: 0.60          # 저심박 기준 (HR/HRmax)
    hr_normal_cap: 100           # 정상 심박수 상한
    intensity_on: 60             # 활동 강도 기준점
    hr_ratio_active_max: 0.85    # 활동 중 정상 심박 비율
```

<a id="환경별-설정"></a>

## 🌐 환경별 설정

### 로컬 개발 환경

- **Kafka**: `localhost:9092`
- **MySQL**: SQLAlchemy 기본 설정 사용

### Docker 환경

- **Kafka**: `kafka:9093`
- **MySQL**: Docker 네트워크를 통한 연결

### Kubernetes 환경

- **Deployment**: `kubernetes/deployment.yaml`
- **Service**: `kubernetes/service.yaml`
- AKS 환경에서 운영

<a id="api-명세"></a>

## 📡 API 명세

Health 서비스는 외부 API보다 Kafka를 통한 데이터 파이프라인을 중심으로 동작합니다. 관련 API 및 데이터 형식은 아래 Notion 링크의 'Health' 섹션을 참고하십시오.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)
