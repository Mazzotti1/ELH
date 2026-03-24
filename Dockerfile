# ============================================================
#  ELH — Every Losted History
#  Multi-stage Dockerfile com builder por servico
#
#  Uso via docker-compose:
#    build:
#      context: .
#      target: <nome-do-servico>
# ============================================================

# === STAGE 1: DEPENDENCY CACHE ===
FROM eclipse-temurin:21-jdk-alpine AS deps

RUN apk add --no-cache maven

WORKDIR /build

COPY pom.xml .
COPY libs/elh-commons/pom.xml           libs/elh-commons/pom.xml
COPY services/discord-gateway/pom.xml   services/discord-gateway/pom.xml
COPY services/media-ingestor/pom.xml    services/media-ingestor/pom.xml
COPY services/search-service/pom.xml    services/search-service/pom.xml
COPY services/chat-service/pom.xml      services/chat-service/pom.xml
COPY services/poll-scheduler/pom.xml    services/poll-scheduler/pom.xml
COPY services/notification-service/pom.xml services/notification-service/pom.xml
COPY services/stats-service/pom.xml     services/stats-service/pom.xml
COPY services/api-gateway/pom.xml       services/api-gateway/pom.xml

RUN mvn dependency:go-offline -B 2>/dev/null || true

# === STAGE 2: COMMONS (compartilhado entre todos) ===
FROM deps AS commons
COPY libs/ libs/
RUN mvn install -B -DskipTests -N && \
    mvn install -B -DskipTests -pl libs/elh-commons

# === BUILDERS POR SERVICO ===
FROM commons AS discord-gateway-builder
COPY services/discord-gateway/ services/discord-gateway/
RUN mvn package -B -DskipTests -pl services/discord-gateway

FROM commons AS media-ingestor-builder
COPY services/media-ingestor/ services/media-ingestor/
RUN mvn package -B -DskipTests -pl services/media-ingestor

FROM commons AS search-service-builder
COPY services/search-service/ services/search-service/
RUN mvn package -B -DskipTests -pl services/search-service

FROM commons AS chat-service-builder
COPY services/chat-service/ services/chat-service/
RUN mvn package -B -DskipTests -pl services/chat-service

FROM commons AS poll-scheduler-builder
COPY services/poll-scheduler/ services/poll-scheduler/
RUN mvn package -B -DskipTests -pl services/poll-scheduler

FROM commons AS notification-service-builder
COPY services/notification-service/ services/notification-service/
RUN mvn package -B -DskipTests -pl services/notification-service

FROM commons AS stats-service-builder
COPY services/stats-service/ services/stats-service/
RUN mvn package -B -DskipTests -pl services/stats-service

FROM commons AS api-gateway-builder
COPY services/api-gateway/ services/api-gateway/
RUN mvn package -B -DskipTests -pl services/api-gateway

# === RUNTIME BASE ===
FROM eclipse-temurin:21-jre-alpine AS runtime-base
RUN addgroup -S elh && adduser -S elh -G elh
WORKDIR /app
USER elh
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

# === discord-gateway (8080) ===
FROM runtime-base AS discord-gateway
COPY --from=discord-gateway-builder /build/services/discord-gateway/target/discord-gateway-*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# === media-ingestor (8081) ===
FROM runtime-base AS media-ingestor
COPY --from=media-ingestor-builder /build/services/media-ingestor/target/media-ingestor-*.jar app.jar
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# === search-service (8082) ===
FROM runtime-base AS search-service
COPY --from=search-service-builder /build/services/search-service/target/search-service-*.jar app.jar
EXPOSE 8082
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8082/actuator/health || exit 1

# === chat-service (8083) ===
FROM runtime-base AS chat-service
COPY --from=chat-service-builder /build/services/chat-service/target/chat-service-*.jar app.jar
EXPOSE 8083
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8083/actuator/health || exit 1

# === poll-scheduler (8084) ===
FROM runtime-base AS poll-scheduler
COPY --from=poll-scheduler-builder /build/services/poll-scheduler/target/poll-scheduler-*.jar app.jar
EXPOSE 8084
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8084/actuator/health || exit 1

# === notification-service (8085) ===
FROM runtime-base AS notification-service
COPY --from=notification-service-builder /build/services/notification-service/target/notification-service-*.jar app.jar
EXPOSE 8085
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8085/actuator/health || exit 1

# === stats-service (8086) ===
FROM runtime-base AS stats-service
COPY --from=stats-service-builder /build/services/stats-service/target/stats-service-*.jar app.jar
EXPOSE 8086
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8086/actuator/health || exit 1

# === api-gateway (8000) ===
FROM runtime-base AS api-gateway
COPY --from=api-gateway-builder /build/services/api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8000
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8000/actuator/health || exit 1
