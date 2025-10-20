# ---------- 1) Build stage ----------
FROM gradle:8-jdk21 AS build
WORKDIR /app

# Cache deps
COPY gradle gradle
COPY gradlew settings.gradle.kts build.gradle.kts ./
# If you use Groovy DSL:
# COPY gradlew settings.gradle build.gradle ./
RUN ./gradlew --no-daemon dependencies || true

# Bring in source
COPY . .

# Build fat jar (Spring Boot: bootJar; Ktor/plain: shadowJar)
RUN ./gradlew --no-daemon clean bootJar || ./gradlew --no-daemon clean shadowJar

# ---------- 2) Runtime stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the artifact produced in the named "build" stage above
COPY --from=build /app/build/libs/*.jar /app/app.jar

# Run as non-root
RUN useradd -r -u 10001 appuser
USER appuser

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]

