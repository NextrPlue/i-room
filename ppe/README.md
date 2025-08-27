# PPE (Personal Protective Equipment) Service

> i-room 프로젝트의 개인보호장비 착용 감지 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>
## 📄 서비스 소개

CCTV 영상 프레임을 입력받아, 딥러닝 모델을 통해 근로자의 개인보호장비(PPE) 착용 여부를 실시간으로 탐지하고 분석하는 마이크로서비스입니다.

### 주요 기능

- **CCTV 이미지 분석**: API 엔드포인트를 통해 CCTV 이미지 프레임을 수신합니다.
- **안전장비 착용 감지**: YOLOv8 모델을 사용하여 이미지 내 근로자의 안전모, 안전고리 등 개인보호장비 착용 여부를 탐지합니다.
- **감지 결과 반환**: 탐지된 객체의 정보(클래스, 위치 등)와 위험 여부를 JSON 형태로 반환합니다.

<a id="개발자"></a>
## 🧑‍💻 개발자

|          | 장준혁                                                      | 김민수                                                     |
|----------|-------------------------------------------------------------|------------------------------------------------------------|
| **E-Mail** | kalina01255@naver.com                                       | minsue9608@naver.com                                       |
| **GitHub** | [angrynison](https://github.com/angrynison)                 | [K-Minsu](https://github.com/K-Minsu)                      |
| **Profile**  | <img src="https://github.com/angrynison.png" width=100px>   | <img src="https://github.com/K-Minsu.png" width=100px>     |

<a id="서비스-개발-주안점"></a>
## 💻 서비스 개발 주안점

### 📌 딥러닝 기반 객체 탐지 모델 활용
> YOLOv8와 같은 딥러닝 기반 객체 탐지 모델을 사용하여 CCTV 영상 프레임 내에서 실시간으로 근로자와 안전장비(헬멧 등)를 탐지합니다. 이를 통해 수동적인 감시를 자동화하고 안전 사각지대를 해소하여 사고 예방에 기여합니다.

<a id="시작-가이드"></a>
## 🚀 시작 가이드

### 사전 준비 사항

- Python 3.9 이상
- pip

### 서비스 실행

1.  **프로젝트 클론 및 디렉토리 이동**
    ```bash
    git clone {저장소 URL}
    cd i-room/ppe
    ```

2.  **필요 라이브러리 설치**
    ```bash
    pip install -r requirements.txt
    ```

3.  **FastAPI 서버 실행**
    ```bash
    uvicorn main:app --reload --host 0.0.0.0 --port 8002
    ```
    (`main.py` 파일의 FastAPI 인스턴스 이름이 `app`이라고 가정)

<a id="기술-스택"></a>
## ⚙️ 기술 스택

- **Python**: 프로그래밍 언어
- **FastAPI**: 웹 프레임워크
- **Uvicorn**: ASGI 서버
- **Pydantic**: 데이터 유효성 검사
- **YOLOv8**: 객체 탐지 모델
- **PyTorch**: 딥러닝 프레임워크
- **OpenCV-Python**: 이미지 처리 라이브러리
- **Qdrant-client**: 벡터 데이터베이스 클라이언트

<a id="api-명세"></a>
## 📡 API 명세

PPE 서비스는 이미지 분석을 위한 API 엔드포인트를 제공합니다. 자세한 요청/응답 형식은 아래 Notion 링크의 'PPE' 섹션을 참고하십시오.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)