FROM eclipse-temurin:17-jdk-alpine as build

WORKDIR /workspace/app

ARG ID_LOG_VERSION=1.0.0-SNAPSHOT
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
COPY libs libs
COPY templates templates

ENV sqlms.saved-queries-dir=./templates

RUN ./mvnw install:install-file -Dfile=libs/id-log-${ID_LOG_VERSION}.jar -DgroupId=ee.ria.commons -DartifactId=id-log -Dversion=${ID_LOG_VERSION} -Dpackaging=jar -DgeneratePom=true
RUN ./mvnw install -DskipTests=true

ENTRYPOINT  ["java", "-jar", "./target/sql-ms.war"]
