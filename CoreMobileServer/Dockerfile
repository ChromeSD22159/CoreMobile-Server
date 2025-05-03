# Build-Stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Force cache invalidation
COPY ../gradle ./gradle
COPY ../gradlew ./
RUN chmod +x ./gradlew

COPY CoreMobileServer/ build.gradle.kts
COPY settings.gradle.kts ./

RUN ./gradlew dependencies --write-locks

COPY CoreMobileServer ./CoreMobileServer
COPY Shared ./Shared

RUN ./gradlew :CoreMobileServer:installDist

# 2. Runtime Stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/CoreMobileServer/build/install/CoreMobileServer /app
ENTRYPOINT ["/app/bin/CoreMobileServer"]