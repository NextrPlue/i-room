# Sensor Service

> i-room 프로젝트의 센서 데이터 및 위치 추적 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [🏗️ 아키텍처](#아키텍처)
> - [📋 주요 기능](#주요-기능)
> - [🌐 환경별 설정](#환경별-설정)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>

## 📄 서비스 소개

i-room 서비스의 근로자 센서 데이터와 중장비 위치 추적을 담당하는 마이크로서비스입니다. 웨어러블 디바이스의 바이너리 데이터 처리와 WebSocket을 통한 실시간 위치 정보 전송을 지원합니다.

### 핵심 기능

- **근로자 센서 데이터**: 웨어러블 디바이스의 생체 및 위치 정보 처리
- **중장비 관리**: 건설 현장 중장비 등록 및 위치 추적
- **실시간 전송**: WebSocket을 통한 실시간 위치 데이터 전송
- **이벤트 처리**: Kafka를 통한 위치 및 센서 이벤트 발행

<a id="개발자"></a>

## 🧑‍💻 개발자

|             | 이성훈                                                      | 조승빈                                                         | 박소연                                                     |
|-------------|----------------------------------------------------------|-------------------------------------------------------------|---------------------------------------------------------|
| **E-Mail**  | p.plue1881@gmail.com                                     | benscience@naver.com                                        | gumza9go@gmail.com                                      |
| **GitHub**  | [NextrPlue](https://github.com/NextrPlue)                | [changeme4585](https://github.com/changeme4585)             | [sorasol9](https://github.com/sorasol9)                 |
| **Profile** | <img src="https://github.com/NextrPlue.png" width=100px> | <img src="https://github.com/changeme4585.png" width=100px> | <img src="https://github.com/sorasol9.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 바이너리 데이터 프로토콜을 통한 효율적 센서 데이터 처리

> 웨어러블 디바이스의 센서 데이터를 JSON이 아닌 바이너리 형태로 전송받도록 개발하여, 데이터 전송량 최소화와 배터리 효율성을 극대화했습니다.
>
> 이를 통해 건설현장의 열악한 네트워크 환경에서도 안정적인 실시간 위치 추적과 생체 데이터 수집이 가능하도록 했습니다.

### 📌 안전한 실시간 통신 및 이벤트 기반 데이터 분배

> WebSocket(STOMP)을 통해 근로자 및 중장비의 위치 데이터를 실시간으로 스트리밍하며, WebSocket 연결 단계에서 JWT 기반 인증 및 역할 기반 권한 검증을 수행하여 보안을 강화했습니다.
>
> 또한, 처리된 센서 데이터를 Kafka 이벤트로 발행하여 다른 마이크로서비스들이 실시간으로 데이터를 활용할 수 있도록 개발했습니다.

<a id="시작-가이드"></a>

## 🚀 시작 가이드

### 사전 준비 사항

- Java 17
- Gradle 8.14 이상

### 서비스 실행

1. **프로젝트 클론 및 디렉토리 이동**
   ```bash
   git clone {저장소 URL}
   cd i-room/sensor
   ```

2. **Gradle을 사용하여 애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```
   *Windows의 경우:*
   ```bash
   gradlew.bat bootRun
   ```

3. **데이터베이스 설정** (필수)
   MySQL 데이터베이스가 실행 중이어야 하며, `iroom_sensor` 데이터베이스가 생성되어 있어야 합니다.
   ```sql
   CREATE DATABASE iroom_sensor;
   ```

4. **Kafka 설정** (필수)
   로컬 환경에서 Kafka가 `localhost:9092`에서 실행 중이어야 합니다.

5. **애플리케이션 접속**
   서비스가 정상적으로 실행되면 `http://localhost:8083`에서 서비스가 활성화되고, WebSocket 엔드포인트를 통해 실시간 위치 데이터를 전송할 수 있습니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Java 17**: 프로그래밍 언어
- **Spring Boot 3.5.3**: 애플리케이션 프레임워크
- **Spring WebSocket**: WebSocket 및 STOMP 프로토콜 지원
- **Spring Data JPA**: 데이터베이스 ORM
- **Spring Cloud 2025.0.0**: 마이크로서비스 인프라
- **Spring Cloud Stream**: Kafka 메시징
- **MySQL 8.0**: 관계형 데이터베이스
- **Apache Kafka**: 이벤트 스트리밍
- **Gradle**: 빌드 도구
- **Micrometer**: 메트릭 및 추적

<a id="아키텍처"></a>

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Gateway     │    │  Sensor Service │    │    MySQL DB     │
│                 │◄──►│                 │◄──►│   (iroom_sensor)│
│  - JWT Auth     │    │  - WebSocket    │    │                 │
│  - Routing      │    │  - Equipment    │    │ - worker_sensor │
└─────────────────┘    │  - Worker Data  │    │ - heavy_equip   │
                       └─────────────────┘    │ - worker_read   │
                             ▲  ▲             │   _model        │
         ┌───────────────────┘  │             └─────────────────┘
         ▼                      ▼
┌─────────────────┐    ┌─────────────────┐    ┌───────────────────────┐
│ Wearable Device │    │   Kafka Broker  │    │   WebSocket Clients   │
│                 │    │                 │◄──►│  - Admin Dashboard    │
│  - Binary Data  │    │  Topic: iroom   │    │  - Worker Page        │
│  - GPS/Health   │    │ - WorkerSensor  │    └───────────────────────┘
└─────────────────┘    │ - EquipmentLoc  │
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

### 1. 근로자 센서 데이터 (`/worker-sensor/**`)

- **센서 데이터 업데이트**: `PUT /worker-sensor/update` (바이너리 데이터)
- **위치 조회**: `GET /worker-sensor/location/{workerId}`
- **전체 위치 조회**: `GET /worker-sensor/locations` (실시간 모니터링)

### 2. 중장비 관리 (`/heavy-equipments/**`)

- **중장비 등록**: `POST /heavy-equipments/register`
- **위치 업데이트**: `PUT /heavy-equipments/location` (바이너리 데이터)
- **중장비 조회**: `GET /heavy-equipments/{equipmentId}`
- **전체 중장비 조회**: `GET /heavy-equipments`

### 3. WebSocket 실시간 전송

- **WebSocket 엔드포인트**: `/sensor/ws`
- **위치 데이터 스트리밍**: 실시간 근로자 및 중장비 위치 전송
- **STOMP 프로토콜**: `/topic/locations/{workerId}` 구독

<a id="환경별-설정"></a>

## 🌐 환경별 설정

### 로컬 개발 환경 (기본)

- **Database**: `localhost:3306/iroom_sensor`
- **Kafka**: `localhost:9092`

### Docker 환경 (`docker` 프로필)

- **Database**: `mysql:3306/iroom_sensor`
- **Kafka**: `kafka:9093`

### Kubernetes 환경 (`k8s` 프로필)

- **Database**: `i-room-mysql/iroom_sensor`
- **Kafka**: `i-room-kafka:9092`

<a id="api-명세"></a>

## 📡 API 명세

센서 서비스는 RESTful API와 WebSocket을 통해 통신하며, 바이너리 데이터 처리를 지원합니다. 자세한 API 명세는 아래 Notion 링크의 'Sensor' 섹션을 참고하십시오.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)