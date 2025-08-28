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

- **심박수 및 이동 데이터 수집**: Wear OS 장치의 센서를 활용해 실시간으로 심박수, 걸음 수, 페이스, 분당 걸음 수 등을 수집
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

### 📌 BroadcastReceiver 이슈 해결

> - **문제:** Android 14 정책으로 인한 BroadcastReceiver 미작동
> - **해결:** SharedPreferences를 통한 우회 전략으로 실시간 구현

### 📌 멀티 센서 동시 수집 최적화

> - Health Services의 ExerciseClient로 통합(MeasureClient 대비 안정성 향상) 
> - GPS와 헬스 데이터를 독립적 스레드에서 처리하여 데이터 손실 방지

### 📌 백그라운드 안정성 확보

> - ForegroundService + 지속 알림으로 시스템 종료 방지
> - 5초 주기 위치 업데이트로 정확도와 배터리 효율 균형

### 📌 네트워크 안정성 강화

> - Retrofit Interceptor를 통한 토큰 자동 갱신
> - 바이너리 직렬화(DataOutputStream)로 전송 효율 개선

<a id="시작-가이드"></a>

## 🚀 시작 가이드

### 사전 준비 사항

- Android Studio
- Android Wear OS 5 이상 기기
- 서버 연결을 위한 네트워크 환경

### 서비스 실행

1. **Android Studio에서 프로젝트 열기**
   `i-room/worker-wear-app` 디렉토리를 엽니다.

2. **웨어러블 기기 연결**
   센서가 활성화된 Wear OS 기기 또는 에뮬레이터를 연결합니다.

3. **애플리케이션 실행**
   'Run' 버튼을 클릭하여 앱을 빌드하고 기기에 설치합니다.

4. **권한 설정**
   앱 실행 시 신체 센서 접근 권한 및 위치 정보 접근 권한(포그라운드/백그라운드)을 허용해야 합니다.

5. **서버 설정**
   데이터를 전송할 `Sensor` 서비스의 주소를 네트워크 관련 설정 파일(`ip_config.properties`)에서 수정해야 합니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Language**: Kotlin
- **Platform**: Android Studio (Wear OS)
- **Key Libraries**:
    - `androidx.health.service.client.*`: 운동 데이터 수집(`ExerciseClient`, `ExerciseUpdateCallback`)
    - `com.google.android.gms.location`: GPS 위치 정보 수집(`fusedLocationClient`, `locationCallback`)
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

### 1. 생체 및 운동 센서 데이터 수집

- **심박수(Heart Rate)**: `DataType.HEART_RATE_BPM`
- **걸음 수(Steps)**: `DataType.STEPS`
- **분당 걸음 수(Steps Per Minutes)**: `DataType.STEPS_PER_MINUTE`
- **속도(Speed)**: `DataType.SPEED`
- **페이스(Pace)**: `DataType.PACE`
- `ExerciseUpdateCallback`을 통해 실시간으로 측정값 수신 후 `SharedPreferences`에 저장

### 2. 위치 정보 수집

- `FusedLocationProviderClient`를 활용하여 주기적 GPS 위치(위도, 경도) 수집
- Foreground Service로 구현하여 앱 백그라운드에서도 위치 지속 추적 가능

### 3. 데이터 인코딩 및 전송

- 수집된 모든 센서 데이터를 지정된 바이너리 프로토콜에 따라 인코딩합니다.
- `Sensor` 서비스의 `/worker-sensor/update` API로 인코딩된 데이터를 전송합니다.
