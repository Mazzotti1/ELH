FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="ELH Team"
LABEL service="notification-service"

RUN addgroup -S elh && adduser -S elh -G elh

WORKDIR /app

COPY target/notification-service-*.jar app.jar

RUN chown -R elh:elh /app

USER elh

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
