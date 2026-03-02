FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup --system grove && adduser --system --ingroup grove grove

WORKDIR /app

COPY build/libs/*.jar app.jar

RUN chown -R grove:grove /app
USER grove

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
