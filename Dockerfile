FROM openjdk:17-jdk as build

WORKDIR /workspace/app

ARG ID_LOG_VERSION=1.0.0-SNAPSHOT
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
COPY libs libs
RUN mkdir templates
RUN ./mvnw install:install-file -Dfile=libs/id-log-${ID_LOG_VERSION}.jar -DgroupId=ee.ria.commons -DartifactId=id-log -Dversion=${ID_LOG_VERSION} -Dpackaging=jar -DgeneratePom=true
ENTRYPOINT ["./mvnw","spring-boot:run"]