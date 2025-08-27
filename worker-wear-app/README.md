# Worker Wear App

> i-room 프로젝트의 근로자용 웨어러블 센서 데이터 수집 애플리케이션

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [🏗️ 아키텍처](#아키텍처)
> - [📋 주요 기능](#주요-기능)

<a id="서비스-소개"></a>

## 📄 서비스 소개

i-room 프로젝트의 근로자 안전 모니터링을 위한 안드로이드 웨어러블(Wear OS) 애플리케이션입니다. 근로자가 착용한 웨어러블 기기에서 생체 신호 및 위치 데이터를 실시간으로 수집하여 `Sensor` 서비스로
전송하는 역할을 담당합니다.

### 핵심 기능

- **생체 데이터 수집**: 심박수(Heart Rate), 가속도계, 자이로스코프 센서 데이터 수집
- **위치 데이터 수집**: GPS를 이용한 실시간 위치(위도, 경도) 정보 수집
- **바이너리 데이터 전송**: 수집된 모든 센서 데이터를 바이너리 형식으로 인코딩하여 `Sensor` 서비스로 전송

<a id="개발자"></a>

## 🧑‍💻 개발자

|        | 박소연                                                     |
|--------|---------------------------------------------------------|
| E-Mail | gumza9go@gmail.com                                      |
| GitHub | [sorasol9](https://github.com/sorasol9)                 |
|        | <img src="https://github.com/sorasol9.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 실시간 멀티 센서 데이터 처리

> 다양한 종류의 센서(생체, 위치)로부터 들어오는 데이터를 통합하고, 이를 `Sensor` 서비스의 요구사항에 맞는 바이너리 포맷으로 변환하여 실시간으로 전송하는 데이터 파이프라인을 구축했습니다. Wear OS
> 환경의 제약을 고려하여 배터리 사용을 최적화하는 데 중점을 두었습니다.

<a id="시작-가이드"></a>

## 🚀 시작 가이드

### 사전 준비 사항

- Android Studio
- Android Wear OS 기기 또는 에뮬레이터

### 서비스 실행

1. **Android Studio에서 프로젝트 열기**
   `i-room/worker-wear-app` 디렉토리를 엽니다.

2. **웨어러블 기기 연결**
   센서가 활성화된 Wear OS 기기 또는 에뮬레이터를 연결합니다.

3. **애플리케이션 실행**
   'Run' 버튼을 클릭하여 앱을 빌드하고 기기에 설치합니다.

4. **권한 설정**
   앱 실행 시 신체 센서 및 위치 정보 접근 권한을 허용해야 합니다.

5. **서버 설정**
   데이터를 전송할 `Sensor` 서비스의 주소를 네트워크 관련 설정 파일에서 수정해야 합니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Language**: Kotlin
- **Platform**: Android Wear OS
- **Key Libraries**:
    - `androidx.wear.watchface`: 웨어러블 UI 및 서비스
    - `com.google.android.gms:play-services-location`: GPS 위치 정보 수집
    - `com.squareup.retrofit2:retrofit`: HTTP API 통신

<a id="아키텍처"></a>

## 🏗️ 아키텍처

```
┌────────────────────┐      ┌──────────────────────────┐      ┌─────────────────┐
│ Worker Wear Device │      │      Sensor Service      │      │   Kafka Broker  │
│                    ├─────►│                          ├─────►│                 │
│ - Worker GPS       │      │ - Worker Sensor API      │      │  Topic: iroom   │
│ - Worker Health    │      │ - Binary Data Processing │      │ - Worker Sensor │
└────────────────────┘      └──────────────────────────┘      └─────────────────┘
```

<a id="주요-기능"></a>

## 📋 주요 기능

### 1. 센서 데이터 수집

- **심박수(Heart Rate)**: `Sensor.TYPE_HEART_RATE`를 사용하여 실시간 심박수 측정
- **가속도/자이로스코프**: `Sensor.TYPE_ACCELEROMETER`, `TYPE_GYROSCOPE`로 움직임 데이터 측정
- **GPS**: `FusedLocationProviderClient`를 사용하여 위치 정보 수집

### 2. 데이터 인코딩 및 전송

- 수집된 모든 센서 데이터를 지정된 바이너리 프로토콜에 따라 인코딩합니다.
- `Sensor` 서비스의 `/worker-sensor/update` API로 인코딩된 데이터를 전송합니다.
