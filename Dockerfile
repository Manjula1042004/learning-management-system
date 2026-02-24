# ===========================================
# Multi-stage Dockerfile with memory optimization
# ===========================================

# Stage 1: Build
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn -B dependency:go-offline -q

# Copy source and build (skip tests - tests run in CI)
COPY src src
RUN mvn -B clean package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add a non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port Render expects
EXPOSE 10000

# Run with memory limits to prevent OOM on free Render tier
ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:MinRAMPercentage=50.0", \
    "-Xss512k", \
    "-Dspring.profiles.active=prod", \
    "-Dserver.port=${PORT:-10000}", \
    "-Dserver.address=0.0.0.0", \
    "-jar", "app.jar"]