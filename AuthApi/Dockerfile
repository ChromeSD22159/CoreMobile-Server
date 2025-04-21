# Build-Stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :AuthApi:installDist

# 2. Runtime Stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/AuthApi/build/install/AuthApi /app
ENTRYPOINT ["/app/bin/AuthApi"]