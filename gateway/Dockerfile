FROM openjdk:17-jdk-slim

VOLUME /tmp

COPY build/libs/gateway-0.0.1-SNAPSHOT.jar app.jar

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]