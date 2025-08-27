# Management Service

> i-room 프로젝트의 현장 관리자용 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>
## 📄 서비스 소개

건설 현장 관리자가 근로자의 안전, 장비 상태, 현장 정보 등을 통합적으로 관리하고 모니터링하기 위한 마이크로서비스입니다.

### 주요 기능

- **현장 정보 관리**: 담당하는 건설 현장의 정보를 등록하고 수정합니다.
- **근로자 정보 조회**: 현장에 등록된 전체 근로자의 목록과 현재 안전 상태를 조회합니다.
- **장비 관리**: 현장에 설치된 IoT 센서, CCTV 등의 장비 목록과 상태를 관리합니다.
- **안전 가이드라인 관리**: 현장에서 지켜야 할 안전 수칙 및 가이드라인을 등록하고 관리합니다.

<a id="개발자"></a>
## 🧑‍💻 개발자

|          | 이성훈                                                      | 배소정                                                     |
|----------|-------------------------------------------------------------|------------------------------------------------------------|
| **E-Mail** | p.plue1881@gmail.com                                        | bsj9278@gmail.com                                          |
| **GitHub** | [NextrPlue](https://github.com/NextrPlue)                   | [BaeSJ1](https://github.com/BaeSJ1)                        |
| **Profile**  | <img src="https://github.com/NextrPlue.png" width=100px>    | <img src="https://github.com/BaeSJ1.png" width=100px>      |

<a id="서비스-개발-주안점"></a>
## 💻 서비스 개발 주안점

### 📌 계층형 아키텍처 구조
> **Controller, Service, Repository** 계층을 명확히 분리하여 관심사의 분리와 유지보수성을 향상시켰습니다. 각 계층은 고유의 책임을 가지며, 이를 통해 코드의 재사용성과 테스트 용이성을 높였습니다.

<a id="시작-가이드"></a>
## 🚀 시작 가이드

### 사전 준비 사항

- Java 17
- Gradle 8.14 이상

### 서비스 실행

1.  **프로젝트 클론 및 디렉토리 이동**
    ```bash
    git clone {저장소 URL}
    cd i-room/management
    ```

2.  **Gradle을 사용하여 애플리케이션 실행**
    ```bash
    ./gradlew bootRun
    ```
    *Windows의 경우:*
    ```bash
    gradlew.bat bootRun
    ```

3.  **애플리케이션 접속**
    서비스가 정상적으로 실행되면 `http://localhost:8083` (또는 application.yml에 설정된 포트)에서 서비스가 활성화됩니다.

<a id="기술-스택"></a>
## ⚙️ 기술 스택

- **Java 17**: 프로그래밍 언어
- **Spring Boot**: 애플리케이션 프레임워크
- **Spring Data JPA**: 데이터베이스 연동
- **Spring Security**: 인증 및 인가
- **MySQL**: 데이터베이스
- **Gradle**: 빌드 도구

<a id="api-명세"></a>
## 📡 API 명세

관리 서비스 관련 API 명세는 아래 링크의 'Management' 섹션에서 확인할 수 있습니다.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)
