# Gateway Service

> i-room 프로젝트의 API 게이트웨이 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>

## 📄 서비스 소개

i-room 서비스의 모든 외부 요청에 대한 단일 진입점(Single Point of Entry) 역할을 수행하는 API 게이트웨이입니다. Spring Cloud Gateway를 기반으로 구현되었으며, 인증,
라우팅, 로드 밸런싱 등의 기능을 담당합니다.

### 주요 기능

- **Request Routing**: 들어오는 요청을 적절한 마이크로서비스로 라우팅합니다.
- **Authentication**: JWT 토큰 기반의 사용자 인증을 처리합니다. 각 서비스의 접근 권한(인가)은 해당 마이크로서비스에서 처리합니다.
- **Load Balancing**: 각 마이크로서비스 인스턴스에 대한 부하를 분산합니다.
- **Circuit Breaker**: 특정 서비스의 장애가 전체 시스템으로 전파되는 것을 방지합니다.

<a id="개발자"></a>

## 🧑‍💻 개발자

|        | 이성훈                                                      |
|--------|----------------------------------------------------------|
| E-Mail | p.plue1881@gmail.com                                     |
| GitHub | [NextrPlue](https://github.com/NextrPlue)                |
|        | <img src="https://github.com/NextrPlue.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 MSA 환경의 단일 진입점 역할

> Spring Cloud Gateway를 사용하여 MSA(Microservice Architecture) 환경의 모든 요청에 대한 단일 진입점을 제공합니다. 이를 통해 라우팅, 인증 등 공통 기능을 중앙에서
> 처리하여 각 마이크로서비스의 부담을 줄이고 유지보수성을 향상시켰습니다.

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

3. **애플리케이션 접속**
   서비스가 정상적으로 실행되면 `http://localhost:8000` (또는 application.yml에 설정된 포트)에서 게이트웨이가 활성화됩니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Java 17**: 프로그래밍 언어
- **Spring Boot**: 애플리케이션 프레임워크
- **Spring Cloud Gateway**: API 게이트웨이 구현
- **Gradle**: 빌드 도구
- **JWT**: 인증을 위한 토큰

<a id="api-명세"></a>

## 📡 API 명세

게이트웨이를 통해 노출되는 전체 마이크로서비스의 API 명세는 아래 링크에서 확인할 수 있습니다.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)
