# User Service

> i-room 프로젝트의 사용자 관리 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [🏗️ 아키텍처](#아키텍처)
> - [📋 주요 기능](#주요-기능)
> - [🔐 인증 & 보안](#인증--보안)
> - [🌐 환경별 설정](#환경별-설정)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>

## 📄 서비스 소개

i-room 서비스의 사용자 인증 및 관리를 담당하는 마이크로서비스입니다. 관리자, 근로자, 시스템 계정의 3가지 사용자 유형을 지원하며, JWT 기반 인증과 Kafka 이벤트 발행을 통해 다른 서비스와 연동됩니다.

### 핵심 기능

- **사용자 인증**: JWT 토큰 기반의 로그인/로그아웃 처리
- **사용자 관리**: 관리자/근로자 회원가입, 정보 조회/수정
- **시스템 인증**: 마이크로서비스 간 통신을 위한 시스템 계정 인증
- **이벤트 발행**: 사용자 정보 변경 시 Kafka를 통한 이벤트 전파
- **데이터 검증**: 커스텀 검증 어노테이션을 통한 입력값 검증

<a id="개발자"></a>

## 🧑‍💻 개발자

|        | 이성훈                                                      |
|--------|----------------------------------------------------------|
| E-Mail | p.plue1881@gmail.com                                     |
| GitHub | [NextrPlue](https://github.com/NextrPlue)                |
|        | <img src="https://github.com/NextrPlue.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 계층형 아키텍처 구조

> **Controller, Service, Repository** 계층을 명확히 분리하여 관심사의 분리와 유지보수성을 향상시켰습니다. 각 계층은 고유의 책임을 가지며, 이를 통해 코드의 재사용성과 테스트 용이성을
> 높였습니다.

<a id="시작-가이드"></a>

## 🚀 시작 가이드

### 사전 준비 사항

- Java 17
- Gradle 8.14 이상

### 서비스 실행

1. **프로젝트 클론 및 디렉토리 이동**
   ```bash
   git clone {저장소 URL}
   cd i-room/user
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
   MySQL 데이터베이스가 실행 중이어야 하며, `iroom_user` 데이터베이스가 생성되어 있어야 합니다.
   ```sql
   CREATE DATABASE iroom_user;
   ```

4. **Kafka 설정** (필수)
   로컬 환경에서 Kafka가 `localhost:9092`에서 실행 중이어야 합니다.

5. **애플리케이션 접속**
   서비스가 정상적으로 실행되면 `http://localhost:8081`에서 서비스가 활성화됩니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Java 17**: 프로그래밍 언어
- **Spring Boot 3.5.3**: 애플리케이션 프레임워크
- **Spring Data JPA**: 데이터베이스 ORM
- **Spring Security**: 인증 및 인가
- **Spring Cloud 2025.0.0**: 마이크로서비스 인프라
- **Spring Cloud Stream**: Kafka 메시징
- **JWT (JJWT 0.11.5)**: JSON Web Token 인증
- **MySQL 8.0**: 관계형 데이터베이스
- **Apache Kafka**: 이벤트 스트리밍
- **Gradle**: 빌드 도구
- **Micrometer**: 메트릭 및 추적

<a id="아키텍처"></a>

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     Gateway     │    │   User Service  │    │    MySQL DB     │
│                 │◄──►│                 │◄──►│   (iroom_user)  │
│  - JWT Auth     │    │  - Admin        │    │                 │
│  - Routing      │    │  - Worker       │    │  - admins       │
└─────────────────┘    │  - System       │    │  - workers      │
                       └─────────────────┘    │  - systems      │
                                ▲             └─────────────────┘
                                │
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

### 1. 관리자 관리 (`/admins/**`)

- **회원가입**: `POST /admins/signup`
- **로그인**: `POST /admins/login`
- **정보 조회**: `GET /admins/{adminId}`
- **정보 수정**: `PUT /admins/{adminId}/info`
- **비밀번호 변경**: `PUT /admins/{adminId}/password`
- **역할 변경**: `PUT /admins/{adminId}/role`
- **전체 관리자 조회**: `GET /admins` (페이징)

### 2. 근로자 관리 (`/workers/**`)

- **등록**: `POST /workers/register`
- **로그인**: `POST /workers/login`
- **정보 조회**: `GET /workers/{workerId}` (마스킹 처리)
- **정보 수정**: `PUT /workers/{workerId}/info`
- **비밀번호 변경**: `PUT /workers/{workerId}/password`
- **전체 근로자 조회**: `GET /workers` (페이징)

### 3. 시스템 계정 관리 (`/systems/**`)

- **시스템 인증**: `POST /systems/authenticate`
- **용도**: 마이크로서비스 간 통신 보안

<a id="인증--보안"></a>

## 🔐 인증 & 보안

### JWT 토큰 구조

- **Subject**: 사용자 ID
- **Claims**:
    - `email`: 사용자 이메일
    - `role`: 사용자 역할 (ADMIN, WORKER, SYSTEM)

### 데이터 검증

- **@ValidPassword**: 비밀번호 복잡성 검증
- **@ValidPhone**: 전화번호 형식 검증
- **Spring Validation**: DTO 레벨 입력값 검증

### 보안 기능

- **비밀번호 암호화**: BCrypt 해시 적용
- **개인정보 마스킹**: 근로자 정보 조회 시 민감정보 마스킹
- **CORS 설정**: Cross-Origin 요청 제어

<a id="환경별-설정"></a>

## 🌐 환경별 설정

### 로컬 개발 환경 (기본)

- **Database**: `localhost:3306/iroom_user`
- **Kafka**: `localhost:9092`

### Docker 환경 (`docker` 프로필)

- **Database**: `mysql:3306/iroom_user`
- **Kafka**: `kafka:9093`

### Kubernetes 환경 (`k8s` 프로필)

- **Database**: `i-room-mysql/iroom_user`
- **Kafka**: `i-room-kafka:9092`

<a id="api-명세"></a>

## 📡 API 명세

사용자 계정 서비스 관련 API 명세는 아래 링크의 'User' 섹션에서 확인할 수 있습니다.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)
