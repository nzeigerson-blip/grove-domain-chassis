# ── Stage 1: Build ──────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Cache Gradle wrapper
COPY gradle/ gradle/
COPY gradlew .
RUN chmod +x gradlew && ./gradlew --version

# Cache dependencies
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN ./gradlew dependencies --no-daemon || true

# Build application
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: Runtime ───────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup --system grove && adduser --system --ingroup grove grove

WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

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
