FROM maven:3.5.4-jdk-8-alpine AS MAVEN_IMAGE
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package

FROM openjdk:8-jdk-alpine

RUN mkdir /usr/share/http-log-monitor && touch /tmp/access.log
COPY --from=MAVEN_IMAGE /tmp/target/http-log-monitor.jar /usr/share/http-log-monitor.jar
ENTRYPOINT ["java", "-jar", "/usr/share/http-log-monitor.jar"]