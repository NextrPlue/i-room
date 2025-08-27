# User Service

> i-room 프로젝트의 사용자 관리 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>

## 📄 서비스 소개

i-room 서비스의 사용자에 관련된 모든 기능을 담당하는 마이크로서비스입니다. 회원가입, 로그인, 사용자 정보 관리 등의 핵심 기능을 제공합니다.

### 주요 기능

- **회원가입**: 신규 사용자 등록
- **로그인/로그아웃**: JWT 토큰 기반의 인증 처리
- **사용자 정보 관리**: 사용자 프로필 조회, 수정 및 삭제
- **권한 관리**: 사용자 역할에 따른 접근 제어 (인가)

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
높였습니다.

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

3. **애플리케이션 접속**
   서비스가 정상적으로 실행되면 `http://localhost:8081` (또는 application.yml에 설정된 포트)에서 서비스가 활성화됩니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Java 17**: 프로그래밍 언어
- **Spring Boot**: 애플리케이션 프레임워크
- **Spring Data JPA**: 데이터베이스 연동
- **Spring Security**: 인증 및 인가
- **MySQL**: 데이터베이스
- **Gradle**: 빌드 도구
- **JWT**: 인증을 위한 토큰

<a id="api-명세"></a>

## 📡 API 명세

사용자 서비스 관련 API 명세는 아래 링크의 'User' 섹션에서 확인할 수 있습니다.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)
