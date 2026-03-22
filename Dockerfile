# ============================================================
#  ELH — Every Losted History
#  Multi-stage Dockerfile (build tudo uma vez, runtime por servico)
#
#  Uso via docker-compose:
#    build:
#      context: .
#      target: <nome-do-servico>
# ============================================================

# === STAGE 1: BUILD ===
FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache maven

WORKDIR /build

# Cache de dependencias — copia so os POMs primeiro
COPY pom.xml .
COPY libs/elh-commons/pom.xml libs/elh-commons/pom.xml
COPY services/discord-gateway/pom.xml services/discord-gateway/pom.xml
COPY services/media-ingestor/pom.xml services/media-ingestor/pom.xml
COPY services/search-service/pom.xml services/search-service/pom.xml
COPY services/chat-service/pom.xml services/chat-service/pom.xml
COPY services/poll-scheduler/pom.xml services/poll-scheduler/pom.xml
COPY services/notification-service/pom.xml services/notification-service/pom.xml
COPY services/stats-service/pom.xml services/stats-service/pom.xml
COPY services/api-gateway/pom.xml services/api-gateway/pom.xml

RUN mvn dependency:go-offline -B 2>/dev/null || true

# Copia o codigo e builda
COPY libs/ libs/
COPY services/ services/

RUN mvn package -B -DskipTests -pl libs/elh-commons -am && \
    mvn package -B -DskipTests

# === STAGE 2: RUNTIME BASE ===
FROM eclipse-temurin:21-jre-alpine AS runtime-base
RUN addgroup -S elh && adduser -S elh -G elh
WORKDIR /app
USER elh
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

# === discord-gateway (8080) ===
FROM runtime-base AS discord-gateway
COPY --from=builder /build/services/discord-gateway/target/discord-gateway-*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# === media-ingestor (8081) ===
FROM runtime-base AS media-ingestor
COPY --from=builder /build/services/media-ingestor/target/media-ingestor-*.jar app.jar
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# === search-service (8082) ===
FROM runtime-base AS search-service
COPY --from=builder /build/services/search-service/target/search-service-*.jar app.jar
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8082/actuator/health || exit 1

# === chat-service (8083) ===
FROM runtime-base AS chat-service
COPY --from=builder /build/services/chat-service/target/chat-service-*.jar app.jar
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8083/actuator/health || exit 1

# === poll-scheduler (8084) ===
FROM runtime-base AS poll-scheduler
COPY --from=builder /build/services/poll-scheduler/target/poll-scheduler-*.jar app.jar
EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8084/actuator/health || exit 1

# === notification-service (8085) ===
FROM runtime-base AS notification-service
COPY --from=builder /build/services/notification-service/target/notification-service-*.jar app.jar
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8085/actuator/health || exit 1

# === stats-service (8086) ===
FROM runtime-base AS stats-service
COPY --from=builder /build/services/stats-service/target/stats-service-*.jar app.jar
EXPOSE 8086
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8086/actuator/health || exit 1

# === api-gateway (8000) ===
FROM runtime-base AS api-gateway
COPY --from=builder /build/services/api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8000
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8000/actuator/health || exit 1
