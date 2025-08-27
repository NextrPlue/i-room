# Dashboard Service

> i-room 프로젝트의 대시보드 데이터 제공 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>
## 📄 서비스 소개

대시보드 UI에 필요한 데이터를 통합하고 가공하여 제공하는 마이크로서비스입니다. 여러 마이크로서비스의 데이터를 조합하여 대시보드에 최적화된 형태로 전달하는 BFF(Backend for Frontend) 역할을 수행합니다.

### 주요 기능

- **현장 현황 요약**: 전체 근로자 수, 위험 근로자 수 등 대시보드 핵심 지표를 요약하여 제공합니다.
- **근로자 안전 데이터 조회**: 근로자별 안전 장비 착용 현황, 현재 위치 등의 상세 정보를 제공합니다.
- **CCTV 정보 제공**: 대시보드에 표시할 CCTV의 스트리밍 주소 및 관련 정보를 제공합니다.
- **통계 데이터 제공**: 시간별, 일별 위험 감지 통계 등 차트 표시에 필요한 데이터를 제공합니다.

<a id="개발자"></a>
## 🧑‍💻 개발자

|          | 이성훈                                                      | 조승빈                                                        | 배소정                                                     |
|----------|-------------------------------------------------------------|---------------------------------------------------------------|------------------------------------------------------------|
| **E-Mail** | p.plue1881@gmail.com                                        | benscience@naver.com                                          | bsj9278@gmail.com                                          |
| **GitHub** | [NextrPlue](https://github.com/NextrPlue)                   | [changeme4585](https://github.com/changeme4585)               | [BaeSJ1](https://github.com/BaeSJ1)                        |
| **Profile**  | <img src="https://github.com/NextrPlue.png" width=100px>    | <img src="https://github.com/changeme4585.png" width=100px>   | <img src="https://github.com/BaeSJ1.png" width=100px>      |

<a id="서비스-개발-주안점"></a>
## 💻 서비스 개발 주안점

### 📌 BFF (Backend for Frontend) 패턴 적용
> 대시보드 UI에 최적화된 데이터를 제공하기 위해 BFF 패턴을 적용했습니다. 여러 마이크로서비스의 데이터를 조합하고 가공하여 프론트엔드에 필요한 형태로 제공함으로써, 프론트엔드의 복잡도를 낮추고 통신 횟수를 최소화했습니다.

<a id="시작-가이드"></a>
## 🚀 시작 가이드

### 사전 준비 사항

- Java 17
- Gradle 8.14 이상

### 서비스 실행

1.  **프로젝트 클론 및 디렉토리 이동**
    ```bash
    git clone {저장소 URL}
    cd i-room/dashboard
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
    서비스가 정상적으로 실행되면 `http://localhost:8085` (또는 application.yml에 설정된 포트)에서 서비스가 활성화됩니다.

<a id="기술-스택"></a>
## ⚙️ 기술 스택

- **Java 17**: 프로그래밍 언어
- **Spring Boot**: 애플리케이션 프레임워크
- **Spring Data JPA**: 데이터베이스 연동
- **Spring Cloud OpenFeign**: 선언적 REST 클라이언트 (서비스 간 통신)
- **MySQL**: 데이터베이스
- **Gradle**: 빌드 도구

<a id="api-명세"></a>
## 📡 API 명세

대시보드 서비스 관련 API 명세는 아래 링크의 'Dashboard' 섹션에서 확인할 수 있습니다.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)
