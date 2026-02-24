# Multi-stage Dockerfile with explicit port exposure
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copy pom.xml and dependencies
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source and build
COPY src src
RUN mvn -B clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Expose the port Render expects
EXPOSE 10000

# Run with explicit port binding
ENTRYPOINT ["java", "-jar", "-Dserver.port=${PORT:-10000}", "-Dserver.address=0.0.0.0", "app.jar"]