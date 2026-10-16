FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace/app

ARG ID_LOG_VERSION=1.0.0-SNAPSHOT
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
COPY libs libs
COPY templates templates
COPY .env .env

COPY .env /app/.env
RUN echo BUILDTIME=`date +%s%N | cut -b1-13` >> /app/.env

ENV sqlms.saved-queries-dir=./templates
ENV sqlms.saved-queries-dir=/DSL
RUN mkdir -p DSL/GET DSL/POST

RUN ./mvnw install:install-file -Dfile=libs/id-log-${ID_LOG_VERSION}.jar -DgroupId=ee.ria.commons -DartifactId=id-log -Dversion=${ID_LOG_VERSION} -Dpackaging=jar -DgeneratePom=true
RUN ./mvnw install -DskipTests=true

FROM eclipse-temurin:21-jre-alpine AS runtime
ENV sqlms.saved-queries-dir=/DSL
WORKDIR /workspace/app
COPY --from=build /workspace/app/target/sql-ms.war /workspace/app/target/sql-ms.war
COPY --from=build /app/.env /app/.env
USER 1000:1000
ENTRYPOINT ["java", "-jar", "/workspace/app/target/sql-ms.war"]

# Exercise the final runtime as its non-root user with an in-memory datasource.
FROM runtime AS test
USER 0:0
RUN mkdir -p /DSL/backoffice/GET /DSL/backoffice/POST && printf 'SELECT 1 AS smoke_value;\n' > /DSL/backoffice/GET/smoke.sql && chmod -R a+rX /DSL
COPY ci/central/startup-smoke.sh /tmp/startup-smoke.sh
USER 1000:1000
RUN --network=none sh /tmp/startup-smoke.sh
