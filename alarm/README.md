# Alarm Service

> i-room 프로젝트의 실시간 위험 알림 서비스

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

i-room 서비스의 실시간 알람 관리 및 WebSocket 통신을 담당하는 마이크로서비스입니다. PPE 서비스와의 API 통신, Kafka 이벤트 수신, WebSocket을 통한 실시간 알림 전송을 처리합니다.

### 핵심 기능

- **실시간 알림**: WebSocket(STOMP)을 통한 실시간 알림 전송
- **PPE 연동**: PPE 서비스로부터 안전 위반 알림 수신
- **이벤트 처리**: Kafka를 통한 시스템 이벤트 수신 및 처리
- **알림 관리**: 근로자별 알림 기록 조회 및 관리

<a id="개발자"></a>

## 🧑‍💻 개발자

|             | 이성훈                                                      | 배소정                                                   |
|-------------|----------------------------------------------------------|-------------------------------------------------------|
| **E-Mail**  | p.plue1881@gmail.com                                     | bsj9278@gmail.com                                     |
| **GitHub**  | [NextrPlue](https://github.com/NextrPlue)                | [BaeSJ1](https://github.com/BaeSJ1)                   |
| **Profile** | <img src="https://github.com/NextrPlue.png" width=100px> | <img src="https://github.com/BaeSJ1.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 이벤트 기반 실시간 통신

> Kafka를 통해 비동기적으로 이벤트를 수신하고, WebSocket(STOMP)을 통해 클라이언트에게 실시간으로 알림을 전파하는 이벤트 기반 아키텍처를 구현했습니다.
>
> 이를 통해 시스템 간의 결합도를 낮추고 확장성을 확보했습니다.

### 📌 개인화된 실시간 알림 시스템

> WebSocket(STOMP)을 통해 관리자에게는 전체 알림을, 해당 근로자에게는 개인화된 알림을 실시간으로 전송합니다.
>
> WebSocket 연결 및 구독 단계에서 JWT 기반 인증과 역할 기반 권한 검증을 수행하여, 민감한 안전 알림 정보가 안전하게 전달되도록 보장했습니다.

### 📌 안전한 실시간 통신

> WebSocket(STOMP) 연결 단계에서 JWT 기반 인증 및 역할 기반 권한 검증을 수행하여 보안을 강화했습니다.

<a id="시작-가이드"></a>

## 🚀 시작 가이드

### 사전 준비 사항

- Java 17
- Gradle 8.14 이상

### 서비스 실행

1. **프로젝트 클론 및 디렉토리 이동**
   ```bash
   git clone {저장소 URL}
   cd i-room/alarm
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
   MySQL 데이터베이스가 실행 중이어야 하며, `iroom_alarm` 데이터베이스가 생성되어 있어야 합니다.
   ```sql
   CREATE DATABASE iroom_alarm;
   ```

4. **Kafka 설정** (필수)
   로컬 환경에서 Kafka가 `localhost:9092`에서 실행 중이어야 합니다.

5. **애플리케이션 접속**
   서비스가 정상적으로 실행되면 `http://localhost:8084`에서 서비스가 활성화되고, 클라이언트는 WebSocket 엔드포인트 `/alarm/ws`를 통해 실시간 알림을 수신할 수 있습니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Java 17**: 프로그래밍 언어
- **Spring Boot 3.5.3**: 애플리케이션 프레임워크
- **Spring WebSocket**: WebSocket 및 STOMP 프로토콜 지원
- **Spring Data JPA**: 데이터베이스 ORM
- **Spring Security**: 인증 및 인가
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
│     Gateway     │    │  Alarm Service  │    │    MySQL DB     │
│                 │◄──►│                 │◄──►│   (iroom_alarm) │
│  - JWT Auth     │    │  - WebSocket    │    │                 │
│  - Routing      │    │  - API Handler  │    │  - alarms       │
└─────────────────┘    │  - Event Proc   │    │  - worker_read  │
                       └─────────────────┘    │    _model       │
                             ▲  ▲             └─────────────────┘
         ┌───────────────────┘  │
         ▼                      ▼
┌─────────────────┐    ┌─────────────────┐    ┌───────────────────────┐
│   PPE Service   │    │   Kafka Broker  │    │   WebSocket Clients   │
│                 │    │                 │◄──►│  - Admin Dashboard    │
│  - Safety AI    │    │  Topic: iroom   │    │  - Worker Page        │
│  - Detection    │    │  - AlarmEvent   │    └───────────────────────┘
└─────────────────┘    │  - WorkerEvent  │
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

### 1. WebSocket 실시간 알림

- **WebSocket 엔드포인트**: `/alarm/ws`
- **STOMP 프로토콜**: `/topic/alarms/{workerId}` 구독
- **SockJS 지원**: WebSocket 미지원 환경에서 fallback

### 2. PPE API 연동 (`/alarms/**`)

- **PPE 알림 수신**: `POST /alarms/ppe`
- **근로자 알림 조회**: `GET /alarms/workers/me` (페이징)

### 3. Kafka 이벤트 처리

- **Topic**: `iroom`
- **Consumer Group**: `alarm-service`
- **이벤트 타입**: AlarmEvent, WorkerEvent 처리

<a id="환경별-설정"></a>

## 🌐 환경별 설정

### 로컬 개발 환경 (기본)

- **Database**: `localhost:3306/iroom_alarm`
- **Kafka**: `localhost:9092`

### Docker 환경 (`docker` 프로필)

- **Database**: `mysql:3306/iroom_alarm`
- **Kafka**: `kafka:9093`

### Kubernetes 환경 (`k8s` 프로필)

- **Database**: `i-room-mysql/iroom_alarm`
- **Kafka**: `i-room-kafka:9092`

<a id="api-명세"></a>

## 📡 API 명세

알림 서비스는 WebSocket을 통한 실시간 알림과 RESTful API를 제공합니다. 자세한 내용은 아래 Notion 링크의 'Alarm' 섹션을 참고하십시오.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)
