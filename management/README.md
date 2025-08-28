# Management Service

> i-room 프로젝트의 근로자 관리

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

i-room 서비스의 근로자 출입 관리와 안전교육 이수 관리를 담당하는 마이크로서비스입니다. 현장 근로자의 출입 기록과 교육 현황을 실시간으로 관리하며, Kafka를 통해 다른 서비스와 연동됩니다.

### 핵심 기능

- **출입 관리**: 근로자 체크인/체크아웃 및 근무 현황 추적
- **안전교육 관리**: 근로자 안전교육 이수 기록 및 인증서 관리
- **통계 및 모니터링**: 현장 근무 통계 및 실시간 근로자 현황
- **이벤트 연동**: 근로자 정보 변경에 대한 Kafka 이벤트 처리

<a id="개발자"></a>

## 🧑‍💻 개발자

|             | 이성훈                                                      | 배소정                                                   |
|-------------|----------------------------------------------------------|-------------------------------------------------------|
| **E-Mail**  | p.plue1881@gmail.com                                     | bsj9278@gmail.com                                     |
| **GitHub**  | [NextrPlue](https://github.com/NextrPlue)                | [BaeSJ1](https://github.com/BaeSJ1)                   |
| **Profile** | <img src="https://github.com/NextrPlue.png" width=100px> | <img src="https://github.com/BaeSJ1.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 근로자 정보 통합관리

> 근로자의 현장 출입 기록과 안전 교육 이수 현황을 통합적으로 관리하여, 현장 관리자가 근로자의 안전 관련 이력을 한눈에 파악할 수 있도록 개발하였습니다.
>
> 이를 통해 실시간 근무 현황 및 통계 데이터를 제공하여 효율적인 현장 운영 의사결정을 돕습니다.

<a id="시작-가이드"></a>

## 🚀 시작 가이드

### 사전 준비 사항

- Java 17
- Gradle 8.14 이상

### 서비스 실행

1. **프로젝트 클론 및 디렉토리 이동**
   ```bash
   git clone {저장소 URL}
   cd i-room/management
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
   MySQL 데이터베이스가 실행 중이어야 하며, `iroom_management` 데이터베이스가 생성되어 있어야 합니다.
   ```sql
   CREATE DATABASE iroom_management;
   ```

4. **Kafka 설정** (필수)
   로컬 환경에서 Kafka가 `localhost:9092`에서 실행 중이어야 합니다.

5. **애플리케이션 접속**
   서비스가 정상적으로 실행되면 `http://localhost:8082`에서 서비스가 활성화됩니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Java 17**: 프로그래밍 언어
- **Spring Boot 3.5.3**: 애플리케이션 프레임워크
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
│     Gateway     │    │   Management    │    │    MySQL DB     │
│                 │◄──►│    Service      │◄──►│   (iroom_       │
│  - JWT Auth     │    │                 │    │   management)   │
│  - Routing      │    │  - Entry Mgmt   │    │                 │
└─────────────────┘    │  - Education    │    │ - worker_mgmt   │
                       │  - Stats        │    │ - worker_edu    │
                       └─────────────────┘    │ - worker_read   │
                                ▲             │   _model        │
                                │             └─────────────────┘
                                ▼
                       ┌─────────────────┐
                       │   Kafka Broker  │
                       │                 │
                       │  Topic: iroom   │
                       │  - WorkerEvent  │
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

### 1. 근로자 출입 관리 (`/entries/**`)

- **체크인**: `POST /entries/{workerId}/check-in`
- **체크아웃**: `POST /entries/{workerId}/check-out`
- **출입 기록 조회**: `GET /entries/{workerId}`
- **전체 출입 현황**: `GET /entries` (페이징, 날짜 필터링)
- **근무 통계**: `GET /entries/stats` (일별/월별 통계)
- **현재 근무자**: `GET /entries/working` (실시간 근무 중인 근로자)

### 2. 안전교육 관리 (`/worker-education/**`)

- **교육 이수 등록**: `POST /worker-education`
- **교육 기록 조회**: `GET /worker-education/{workerId}`
- **전체 교육 현황**: `GET /worker-education` (페이징)

<a id="환경별-설정"></a>

## 🌐 환경별 설정

### 로컬 개발 환경 (기본)

- **Database**: `localhost:3306/iroom_management`
- **Kafka**: `localhost:9092`

### Docker 환경 (`docker` 프로필)

- **Database**: `mysql:3306/iroom_management`
- **Kafka**: `kafka:9093`

### Kubernetes 환경 (`k8s` 프로필)

- **Database**: `i-room-mysql/iroom_management`
- **Kafka**: `i-room-kafka:9092`

<a id="api-명세"></a>

## 📡 API 명세

근로자 관리 서비스 관련 API 명세는 아래 링크의 'Management' 섹션에서 확인할 수 있습니다.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)
