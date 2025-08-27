# Sensor Service

> i-room 프로젝트의 IoT 센서 데이터 처리 서비스

> 목차
> - [📄 서비스 소개](#서비스-소개)
> - [🧑‍💻 개발자](#개발자)
> - [💻 서비스 개발 주안점](#서비스-개발-주안점)
> - [🚀 시작 가이드](#시작-가이드)
> - [⚙️ 기술 스택](#기술-스택)
> - [📡 API 명세](#api-명세)

<a id="서비스-소개"></a>

## 📄 서비스 소개

건설 현장의 다양한 IoT 장비로부터 센서 데이터를 수신하여 처리하고, Kafka 토픽으로 전송하는 역할을 담당하는 마이크로서비스입니다.

### 주요 기능

- **센서 데이터 수신**: Netty 기반의 TCP 소켓 통신을 통해 비동기적으로 센서 데이터를 수신합니다.
- **데이터 파싱 및 처리**: 수신된 바이너리 데이터를 정형화된 데이터로 파싱하고 가공합니다.
- **Kafka 이벤트 발행**: 처리된 데이터를 실시간 분석 및 모니터링을 위해 Kafka 토픽으로 발행합니다.

<a id="개발자"></a>

## 🧑‍💻 개발자

|             | 이성훈                                                      | 조승빈                                                         | 박소연                                                     |
|-------------|----------------------------------------------------------|-------------------------------------------------------------|---------------------------------------------------------|
| **E-Mail**  | p.plue1881@gmail.com                                     | benscience@naver.com                                        | gumza9go@gmail.com                                      |
| **GitHub**  | [NextrPlue](https://github.com/NextrPlue)                | [changeme4585](https://github.com/changeme4585)             | [sorasol9](https://github.com/sorasol9)                 |
| **Profile** | <img src="https://github.com/NextrPlue.png" width=100px> | <img src="https://github.com/changeme4585.png" width=100px> | <img src="https://github.com/sorasol9.png" width=100px> |

<a id="서비스-개발-주안점"></a>

## 💻 서비스 개발 주안점

### 📌 Netty 기반 소켓 통신 및 비동기 처리

> Netty 프레임워크를 사용하여 비동기 이벤트 기반의 TCP/IP 소켓 통신을 구현하여, 다수의 IoT 장비로부터 안정적으로 대량의 센서 데이터를 수신합니다. 또한, 수신한 데이터를 Kafka에 발행함으로써,
후속 데이터 처리 서비스(Health, Dashboard 등)와의 결합도를 낮추고 시스템 전체의 확장성과 탄력성을 확보했습니다.

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

3. **애플리케이션 접속**
   서비스가 정상적으로 실행되면 `application.yml` 에 설정된 TCP 포트에서 센서 데이터 수신을 시작합니다.

<a id="기술-스택"></a>

## ⚙️ 기술 스택

- **Java 17**: 프로그래밍 언어
- **Spring Boot**: 애플리케이션 프레임워크
- **Netty**: 비동기 소켓 통신 프레임워크
- **Spring for Apache Kafka**: Kafka 연동
- **Gradle**: 빌드 도구

<a id="api-명세"></a>

## 📡 API 명세

센서 서비스는 REST API 대신 TCP 소켓을 통해 통신합니다. 자세한 데이터 프로토콜은 아래 Notion 링크의 'Sensor' 섹션을 참고하십시오.

- [i-room API 명세서 (Notion)](https://disco-mitten-e75.notion.site/API-238f6cd45c7380209227f1f66bddebdd?pvs=73)