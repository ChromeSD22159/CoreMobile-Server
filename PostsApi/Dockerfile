# Build-Stage
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :PostsApi:installDist

# 2. Runtime Stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/PostsApi/build/install/PostsApi /app
ENTRYPOINT ["/app/bin/PostsApi"]