# i-room
| 지능형 건설현장 근로자 안전 통합관리 플랫폼 "이룸(I-Room)"
> 목차
> - [📌 프로젝트 소개](#프로젝트-소개)
> - [👩‍👩‍👧‍👧 팀원 소개](#팀원-소개)
> - [✏️ 주요 기능](#주요-기능)
> - [🔗 링크 모음](#링크-모음)
> - [📡 API 명세](#api-명세)
> - [🚩 시작 가이드](#시작-가이드)
> - [🖥️ 기술 스택](#기술-스택)
> - [📁 프로젝트 구조](#프로젝트-구조)

<br>

## 프로젝트 소개

### 개발 동기 및 목적 (수정 필요)

고용노동부 자료에 따르면, OECD 38개국 근로자 10,000명당 산재사고 사망자 수 는 0.43명으로 평균 0.29명을 웃도는 수치이면서, 5등에 속한다.[연합뉴스]

올해 1월 27일부터 중대재해처벌법이 시행되고 있지만 안전사고 사망자는 오히려 늘었다. 현장 근로자의 사망만인율은 선진국의 4배 수준, 현장 안전관리의 최일선을 책임지는 감리원에 대한 관리·감독이 부실하게 이뤄지고 있음.[동아일보]

→ 중대재해 처벌법이 시행되고 있음에도, 현장 근로자의 사고율은 계속 증가하고 있음, 현장 감리원의 부실한 관리·감독을 해결하고 사고 방지를 위해 플랫폼을 개발하고자 함. 현장의 CCTV 데이터를 활용해 근로자의 사고위험을 분석하고 관리하는 통합 플랫폼을 개발

<br>

### 서비스 소개

> 지능형 건설현장 근로자 안전 통합관리 플랫폼 '이룸(I-Room)'

1. ✏️ **기능명**
    - 간략한 기능 내용

<br>

### 개발 기간

2025.07.18 ~ 2025.09.01

<br>

## 팀원 소개

|        | 이성훈                                                      | 김민수                                                    | 박소연                                                     | 배소정                                                   | 장준혁                                                       | 조승빈                                                         |
|--------|----------------------------------------------------------|--------------------------------------------------------|---------------------------------------------------------|-------------------------------------------------------|-----------------------------------------------------------|-------------------------------------------------------------|
| 역할     |                                                          |                                                        |                                                         |                                                       |                                                           |                                                             |
| E-Mail | p.plue1881@gmail.com                                     | minsue9608@naver.com                                   | gumza9go@gmail.com                                      | bsj9278@gmail.com                                     | kalina01255@naver.com                                     | benscience@naver.com                                        |
| GitHub | https://github.com/NextrPlue                             | https://github.com/K-Minsu                             | https://github.com/sorasol9                             | https://github.com/BaeSJ1                             | https://github.com/angrynison                             | https://github.com/changeme4585                             |
|        | <img src="https://github.com/NextrPlue.png" width=100px> | <img src="https://github.com/K-Minsu.png" width=100px> | <img src="https://github.com/sorasol9.png" width=100px> | <img src="https://github.com/BaeSJ1.png" width=100px> | <img src="https://github.com/angrynison.png" width=100px> | <img src="https://github.com/changeme4585.png" width=100px> |

<br>

## 주요 기능

> - 기능

<table>
  <tr>
    <th>기능</th>
    <th>설명</th>
  </tr>
  <tr>
    <td><b>기능명</b></td>
    <td>내용<br>
        내용</td>
  </tr>
</table>

<br>

## Frontend 개발 주안점

### 📌 계층형 아키텍처 구조

> **Controller, Service, Repository** 계층을 명확히 분리하여 관심사의 분리와 유지보수성을 향상시켰습니다.

<br>

## Backend 개발 주안점

### 📌 계층형 아키텍처 구조

> **Controller, Service, Repository** 계층을 명확히 분리하여 관심사의 분리와 유지보수성을 향상시켰습니다.

<br>

## AI 개발 주안점

### 📌 계층형 아키텍처 구조

> **Controller, Service, Repository** 계층을 명확히 분리하여 관심사의 분리와 유지보수성을 향상시켰습니다.

<br>

## 링크 모음

| 기획 |                                                          디자인                                                          | 개발 |
|:--:|:---------------------------------------------------------------------------------------------------------------------:|:--:|
|    | [와이어프레임](https://www.figma.com/design/YoPKzEsC8NfWVUHoTaZPi0/%EC%9D%B4%EB%A3%B8?node-id=0-1&p=f&t=qhCswsEEfx0Y42VE-0) |    |

<br>

## API 명세

링크 및 이미지 추가 필요

<br>

## 시작 가이드

### 사전 준비 사항

서비스를 실행하기 전에 다음 도구들이 설치되어 있는지 확인하십시오
*   Docker 및 Docker Compose
*   Node.js (Docker를 사용하지 않고 프론트엔드 개발/빌드 시)
*   Maven (Docker를 사용하지 않고 백엔드 개발/빌드 시)

### Docker Compose로 모든 서비스 실행하기

모든 마이크로서비스(백엔드, 게이트웨이, 프론트엔드) 및 Kafka를 시작하려면

1.  **Docker 이미지 빌드 및 서비스 시작**
    ```bash
    docker-compose -f build-docker-compose.yml up --build
    ```
    이 명령어는 필요한 모든 Docker 이미지를 빌드한 다음 `build-docker-compose.yml`에 정의된 서비스를 시작합니다.

2.  **애플리케이션 접속**
    모든 서비스가 실행되면 브라우저에서 프론트엔드 애플리케이션에 접속할 수 있습니다
    ```
    http://localhost:8088
    ```

### 개별 서비스 실행하기 (개발용)

각 마이크로서비스 디렉토리 내의 `README.md` 파일을 참조하십시오
*   `마이크로 서비스 명`
*   `마이크로 서비스 명`

#### API 게이트웨이 실행 (Spring Gateway)
```bash
cd gateway
mvn spring-boot:run
```

#### 프론트엔드 실행
```bash
cd frontend
npm install
npm start
```

### 유틸리티

*   **httpie** (curl / POSTMAN 대체 도구) 및 네트워크 유틸리티
    ```bash
    sudo apt-get update
    sudo apt-get install net-tools
    sudo apt install iputils-ping
    pip install httpie
    ```

*   **kubernetes 유틸리티 (kubectl)**
    ```bash
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
    sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
    ```

<br>

## 기술 스택

### Backend Framework
- **Spring Boot**: 애플리케이션 프레임워크
- **Spring Data JPA**: 데이터 액세스 레이어
- **Spring Web**: RESTful API 구현

### Database
- **H2 Database**: 인메모리 데이터베이스
- **JPA/Hibernate**: ORM 프레임워크

### Development Tools
- **Java 17**: 프로그래밍 언어
- **Gradle 8.14**: 빌드 도구
- **Lombok**: 코드 생성 자동화

<br>

## 시연 영상
