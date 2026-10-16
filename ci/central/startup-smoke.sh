#!/bin/sh
set -eu
test -r /app/.env
grep -q '^BUILDTIME=[0-9]' /app/.env
export SQLMS_PROJECT_DATASOURCES=true SQLMS_DATASOURCES_0_NAME=backoffice
export SQLMS_DATASOURCES_0_JDBCURL="jdbc:h2:mem:smoke;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1"
export SQLMS_DATASOURCES_0_DRIVERCLASSNAME=org.h2.Driver
export SQLMS_DATASOURCES_0_USERNAME=h2 SQLMS_DATASOURCES_0_PASSWORD=h2
java -jar /workspace/app/target/sql-ms.war > /tmp/resql-startup.log 2>&1 &
app_pid=$!
trap 'kill "$app_pid" 2>/dev/null || true; wait "$app_pid" 2>/dev/null || true' EXIT
attempt=0
while [ "$attempt" -lt 90 ]; do
  if ! kill -0 "$app_pid" 2>/dev/null; then
    cat /tmp/resql-startup.log
    exit 1
  fi
  if wget -q -O /tmp/resql-datasources.json http://127.0.0.1:8082/datasources 2>/dev/null; then
    if ! wget -q -O /tmp/resql-smoke.json http://127.0.0.1:8082/backoffice/smoke; then
      cat /tmp/resql-startup.log
      exit 1
    fi
    if ! grep -q '"smokeValue":1' /tmp/resql-smoke.json; then
      cat /tmp/resql-smoke.json
      exit 1
    fi
    printf '%s\n' 'PASS: final Resql runtime started non-root and executed an in-memory SQL query.'
    exit 0
  fi
  attempt=$((attempt + 1))
  sleep 1
done
cat /tmp/resql-startup.log
exit 1
