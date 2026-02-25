# Multi-stage Dockerfile with memory optimization
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn clean package -DskipTests

# Runtime stage with memory limits
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 10000

# Run with strict memory limits
ENTRYPOINT ["java", \
    "-Xmx256m", \
    "-Xss256k", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-XX:MaxRAMPercentage=50.0", \
    "-Djava.awt.headless=true", \
    "-Dfile.encoding=UTF-8", \
    "-jar", "app.jar"]