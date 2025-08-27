# Gateway Service

> i-room 프로젝트의 API 게이트웨이 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [🏗️ 아키텍처](#아키텍처)
> - [🔐 인증 & 라우팅](#인증--라우팅)
> - [🌐 환경별 설정](#환경별-설정)

<a id="서비스-소개"></a>

## 📄 서비스 소개

i-room 서비스의 모든 외부 요청에 대한 단일 진입점(Single Point of Entry) 역할을 수행하는 API 게이트웨이입니다. Spring Cloud Gateway를 기반으로 구현되었으며, JWT 기반
인증과 요청 라우팅을 담당합니다.

### 핵심 기능

- **Request Routing**: 들어오는 요청을 적절한 마이크로서비스로 라우팅
- **JWT Authentication**: JWT 토큰 기반 사용자 인증 및 토큰 검증
- **Header Injection**: 인증된 사용자 정보를 헤더에 주입하여 마이크로서비스로 전달
- **Path Rewriting**: `/api/{service}/**` 형태의 요청을 각 서비스로 라우팅
- **CORS 처리**: 프론트엔드 애플리케이션(관리자/근로자 페이지)와의 통신 지원

<a id="개발자"></a>

## 🧑‍💻 개발자

|        | 이성훈                                                      |
|--------|----------------------------------------------------------|
| E-Mail | p.plue1881@gmail.com                                     |
| GitHub | [NextrPlue](https://github.com/NextrPlue)                |
|        | <img src="https://github.com/NextrPlue.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 MSA 환경의 통합 API 관리

> Spring Cloud Gateway를 사용하여 MSA(Microservice Architecture) 환경의 모든 요청에 대한 단일 진입점을 제공합니다.
>
> 이를 통해 라우팅, 인증 등 공통 기능을 중앙에서 처리하여 각 마이크로서비스의 부담을 줄이고 유지보수성을 향상시켰습니다.

### 📌 인증/인가 책임 분리 및 마이크로서비스 자율성 보장

> 게이트웨이에서는 JWT 기반의 통합 인증만을 담당하고, 실제 리소스에 대한 접근 제어(인가)는 각 마이크로서비스가 자체적으로 수행하도록 설계했습니다.
>
> 이를 통해 각 마이크로서비스가 비즈니스 로직에 맞는 세밀한 인가 정책을 독립적으로 관리할 수 있도록 하여 서비스의 자율성과 확장성을 높였습니다.

<a id="시작-가이드"></a>

## 🚀 시작 가이드

### 사전 준비 사항

- Java 17
- Gradle 8.14 이상

### 서비스 실행

1. **프로젝트 클론 및 디렉토리 이동**
   ```bash
   git clone {저장소 URL}
   cd i-room/gateway
   ```

2. **Gradle을 사용하여 애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```
   *Windows의 경우:*
   ```bash
   gradlew.bat bootRun
   ```

3. **환경변수 설정** (필수)
   ```bash
   export JWT_SECRET=your-jwt-secret-key-here
   export JWT_EXPIRATION=86400000
   ```
   *Windows의 경우:*
   ```bash
   set JWT_SECRET=your-jwt-secret-key-here
   set JWT_EXPIRATION=86400000
   ```

4. **애플리케이션 접속**
   서비스가 정상적으로 실행되면 `http://localhost:8080`에서 게이트웨이가 활성화됩니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Java 17**: 프로그래밍 언어
- **Spring Boot 3.5.3**: 애플리케이션 프레임워크
- **Spring Cloud Gateway**: API 게이트웨이 구현 (WebFlux 기반)
- **Spring Cloud 2025.0.0**: 마이크로서비스 인프라
- **JWT (JJWT 0.11.5)**: JSON Web Token 기반 인증
- **Gradle**: 빌드 도구
- **Micrometer**: 메트릭 및 추적

<a id="아키텍처"></a>

## 🏗️ 아키텍처

```
Frontend Apps (React)          Gateway Service               Backend Services
┌─────────────────┐           ┌─────────────────┐           ┌─────────────────┐
│  Admin App      │◄─────────►│                 │◄─────────►│  User Service   │
│  (port: 3000)   │           │   Gateway       │           │  (port: 8081)   │
└─────────────────┘           │  (port: 8080)   │           └─────────────────┘
                              │                 │           ┌─────────────────┐
┌─────────────────┐           │  - JWT Auth     │◄─────────►│  Management     │
│  Worker App     │◄─────────►│  - CORS         │           │  (port: 8082)   │
│  (port: 3001)   │           │  - Routing      │           └─────────────────┘
└─────────────────┘           │  - Path Rewrite │           ┌─────────────────┐
                              │                 │◄─────────►│  Sensor Service │
                              │                 │           │  (port: 8083)   │
                              │                 │           └─────────────────┘
                              │                 │           ┌─────────────────┐
                              │                 │◄─────────►│  Alarm Service  │
                              │                 │           │  (port: 8084)   │
                              │                 │           └─────────────────┘
                              │                 │           ┌─────────────────┐
                              │                 │◄─────────►│  Dashboard      │
                              │                 │           │  (port: 8085)   │
                              └─────────────────┘           └─────────────────┘
```

<a id="인증--라우팅"></a>

## 🔐 인증 & 라우팅

### JWT 인증 필터

- **공개 경로**: 인증 없이 접근 가능한 엔드포인트
    - `/api/user/actuator`
    - `/api/user/admins/login`
    - `/api/user/admins/signup`
    - `/api/user/workers/login`
    - `/api/user/systems/authenticate`

- **인증 헤더 주입**: 유효한 JWT 토큰에서 추출한 사용자 정보를 헤더로 전달
    - `X-User-Id`: 사용자 ID
    - `X-User-Email`: 사용자 이메일
    - `X-User-Role`: 사용자 역할

### 라우팅 규칙

| 경로 패턴                | 대상 서비스             | 포트   | 설명             |
|----------------------|--------------------|------|----------------|
| `/api/user/**`       | user-service       | 8081 | 사용자 인증 및 관리    |
| `/api/management/**` | management-service | 8082 | 근로자 교육 및 관리    |
| `/api/sensor/**`     | sensor-service     | 8083 | 센서 데이터 및 위치 정보 |
| `/api/alarm/**`      | alarm-service      | 8084 | 알람 시스템         |
| `/api/dashboard/**`  | dashboard-service  | 8085 | 대시보드 및 분석      |

<a id="환경별-설정"></a>

## 🌐 환경별 설정

### 로컬 개발 환경 (기본)

- 각 서비스를 `localhost:{port}` 로 연결
- 프론트엔드: `localhost:3000`, `localhost:3001`

### Docker 환경 (`docker` 프로필)

- 서비스 이름으로 연결 (예: `http://user-service:8081`)
- Docker Compose 네트워크 사용

### Kubernetes 환경 (`k8s` 프로필)

- Kubernetes Service 이름으로 연결 (예: `http://user-service`)
- AKS(Azure Kubernetes Service) 환경 지원
