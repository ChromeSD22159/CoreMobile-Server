# Build-Stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Force cache invalidation
COPY ../. .

ENV GRADLE_USER_HOME=/home/gradle/cache_home
RUN ./gradlew :CoreMobileServer:installDist

# 2. Runtime Stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/CoreMobileServer/build/install/CoreMobileServer /app
ENTRYPOINT ["/app/bin/CoreMobileServer"]