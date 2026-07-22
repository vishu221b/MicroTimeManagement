# syntax=docker/dockerfile:1

# ---- Build stage: compile the fat jar ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache dependencies first for faster incremental builds
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Build the application (tests run in CI, not in the image build)
COPY src ./src
RUN mvn -q -B -DskipTests clean package

# ---- Runtime stage: slim JRE ----
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app

# curl is used by the compose healthcheck
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

ENV SPRING_PROFILES_ACTIVE=dev
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
