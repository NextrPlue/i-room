# Health Service

> i-room 프로젝트의 근로자 건강 상태 예측 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>
## 📄 서비스 소개

Kafka로 수신되는 센서 데이터를 기반으로, AI 모델을 사용하여 근로자의 온열질환 등 건강 위험도를 예측하고 결과를 다시 Kafka로 발행하는 마이크로서비스입니다.

### 주요 기능

- **센서 데이터 수신**: Kafka로부터 온도, 습도 등 건강 상태 예측에 필요한 센서 데이터를 구독(Subscribe)합니다.
- **건강 위험도 예측**: AI 모델을 사용하여 실시간으로 근로자의 온열질환 발생 위험도를 예측합니다.
- **예측 결과 발행**: 예측된 위험도 결과를 다른 서비스(알림, 대시보드)에서 사용할 수 있도록 Kafka 토픽으로 발행합니다.

<a id="개발자"></a>
## 🧑‍💻 개발자

|        | 김민수                                                    |
|--------|--------------------------------------------------------|
| E-Mail | minsue9608@naver.com                                   |
| GitHub | https://github.com/K-Minsu                             |
|        | <img src="https://github.com/K-Minsu.png" width=100px> |

<a id="서비스-개발-주안점"></a>
## 💻 서비스 개발 주안점

### 📌 AI 모델 기반 위험도 예측 및 데이터 파이프라인
> 온열질환 예측 AI 모델을 서비스에 통합하여, 실시간 센서 데이터 기반으로 근로자의 건강 위험도를 예측합니다. 또한, Kafka 컨슈머와 프로듀서를 구현하여 Sensor 서비스로부터 데이터를 받아 처리한 후, 분석된 결과를 다시 다른 서비스로 전달하는 안정적인 데이터 파이프라인을 구축했습니다.

<a id="시작-가이드"></a>
## 🚀 시작 가이드

### 사전 준비 사항

- Python 3.9 이상
- pip

### 서비스 실행

1.  **프로젝트 클론 및 디렉토리 이동**
    ```bash
    git clone {저장소 URL}
    cd i-room/health
    ```

2.  **필요 라이브러리 설치**
    ```bash
    pip install -r requirements.txt
    ```

3.  **FastAPI 서버 실행**
    ```bash
    uvicorn app:app --reload --host 0.0.0.0 --port 8001
    ```
    (`app.py` 파일의 FastAPI 인스턴스 이름이 `app`이라고 가정)

<a id="기술-스택"></a>
## ⚙️ 기술 스택

- **Python**: 프로그래밍 언어
- **FastAPI**: 웹 프레임워크
- **Uvicorn**: ASGI 서버
- **SQLAlchemy**: ORM (데이터베이스 연동)
- **Pydantic**: 데이터 유효성 검사
- **kafka-python**: Kafka 연동 라이브러리
- **Scikit-learn**: AI 모델 실행

<a id="api-명세"></a>
## 📡 API 명세

Health 서비스는 외부 API보다 Kafka를 통한 데이터 파이프라인을 중심으로 동작합니다. 관련 API 및 데이터 형식은 아래 Notion 링크의 'Health' 섹션을 참고하십시오.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)
