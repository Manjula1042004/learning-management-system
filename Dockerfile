# Multi-stage Dockerfile for Spring Boot App with Clean Build
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copy pom.xml and force clean download of dependencies
COPY pom.xml .
RUN mvn dependency:purge-local-repository -DactTransitively=false -DreResolve=false && \
    mvn dependency:go-offline -B

# Copy source code and build with clean flag
COPY src src
RUN mvn clean package -DskipTests -U

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]