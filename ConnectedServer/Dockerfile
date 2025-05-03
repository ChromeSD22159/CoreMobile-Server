# Build-Stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Force cache invalidation
COPY ../gradle ./gradle
COPY ../gradlew ./
RUN chmod +x ./gradlew

COPY ConnectedServer/ build.gradle.kts
COPY settings.gradle.kts ./

RUN ./gradlew dependencies --write-locks

COPY ConnectedServer ./ConnectedServer
COPY Shared ./Shared

RUN ./gradlew :ConnectedServer:installDist

# 2. Runtime Stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/ConnectedServer/build/install/ConnectedServer /app
ENTRYPOINT ["/app/bin/ConnectedServer"]