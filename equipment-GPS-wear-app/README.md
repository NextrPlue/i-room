# Equipment GPS Wear App

> i-room 프로젝트의 건설 중장비 GPS 데이터 수집 애플리케이션

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

i-room 프로젝트의 건설 현장 내 중장비 위치를 실시간으로 추적하기 위한 안드로이드 웨어러블(Wear OS) 애플리케이션입니다. 중장비에 부착된 웨어러블 기기에서 위치 데이터를 수집하여 `Sensor` 서비스로
전송하는 역할을 담당합니다.

### 핵심 기능

- **GPS 데이터 수집**: 중장비의 위치 정보(위도, 경도)를 주기적으로 수집
- **실시간 데이터 전송**: 수집된 위치 데이터를 `Sensor` 서비스의 API를 통해 실시간으로 전송
- **장비 식별**: 각 중장비를 고유하게 식별하는 정보 포함

<a id="개발자"></a>

## 🧑‍💻 개발자

|        | 박소연                                                     |
|--------|---------------------------------------------------------|
| E-Mail | gumza9go@gmail.com                                      |
| GitHub | [sorasol9](https://github.com/sorasol9)                 |
|        | <img src="https://github.com/sorasol9.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 안정적인 데이터 수집 및 전송

> 장비에 부착되어 장기간 운영되는 점을 고려하여, 안정적으로 GPS 데이터를 수집하고 네트워크 통신이 불안정한 환경에서도 데이터를 전송할 수 있도록 구현하는 데 중점을 두었습니다. Wear OS의 백그라운드
> 서비스와 재시도 로직을 통해 데이터 유실을 최소화합니다.

<a id="시작-가이드"></a>

## 🚀 시작 가이드

### 사전 준비 사항

- Android Studio
- Android Wear OS 기기 또는 에뮬레이터

### 서비스 실행

1. **Android Studio에서 프로젝트 열기**
   `i-room/equipment-GPS-wear-app` 디렉토리를 엽니다.

2. **웨어러블 기기 연결**
   GPS 센서가 활성화된 Wear OS 기기 또는 에뮬레이터를 연결합니다.

3. **애플리케이션 실행**
   'Run' 버튼을 클릭하여 앱을 빌드하고 기기에 설치합니다.

4. **권한 설정**
   앱 실행 시 위치 정보 접근 권한을 허용해야 합니다. (또한, 필요시 백그라운드 실행을 위해, 설정에서 항상 허용으로 변경합니다.)

5. **서버 설정**
   데이터를 전송할 `Sensor` 서비스의 주소를 네트워크 관련 설정 파일(`ip_config.properties`)에서 수정해야 합니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Language**: Kotlin
- **Platform**: Android Studio (Wear OS)
- **Key Libraries**:
    - `com.google.android.gms.location`: GPS 위치 정보 수집(`fusedLocationClient`, `locationCallback`)
    - `com.squareup.retrofit2:retrofit`: HTTP API 통신

<a id="아키텍처"></a>

## 🏗️ 아키텍처

```
┌───────────────────────┐      ┌──────────────────────────┐      ┌────────────────┐
│ Equipment Wear Device │      │      Sensor Service      │      │  Kafka Broker  │
│                       ├─────►│                          ├─────►│                │
│ - Heavy Equipment GPS │      │ - Heavy Equipment API    │      │  Topic: iroom  │
└───────────────────────┘      │ - Binary Data Processing │      │ - EquipmentLoc │
                               └──────────────────────────┘      └────────────────┘
```

<a id="주요-기능"></a>

## 📋 주요 기능

### 1. GPS 위치 데이터 수집

- `FusedLocationProviderClient`를 활용하여 주기적 GPS 위치(위도, 경도) 수집
- Foreground Service로 구현하여 앱 백그라운드에서도 위치 지속 추적 가능

### 2. 데이터 인코딩 및 전송

- 수집된 모든 센서 데이터를 지정된 바이너리 프로토콜에 따라 인코딩합니다.
- `Sensor` 서비스의 `/heavy-equipments/location` API로 인코딩된 데이터를 전송합니다.

### 3. UI

- 앱의 메인 화면에 현재 수집되고 있는 GPS 좌표를 실시간으로 표시하여 데이터 수집 상태를 직관적으로 확인할 수 있습니다.
