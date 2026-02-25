# ===========================================
# DOCKERFILE FOR RENDER DEPLOYMENT
# ===========================================

# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copy pom.xml and download dependencies (caching layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src src
RUN mvn clean package -DskipTests -B

# Runtime stage - use smaller JRE image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user to run the application (security best practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port Render expects
EXPOSE 10000

# Run the application with memory optimizations for free tier
ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:+UseStringDeduplication", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:MinRAMPercentage=50.0", \
    "-Xss512k", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dserver.port=${PORT:-10000}", \
    "-Dserver.address=0.0.0.0", \
    "-jar", "app.jar"]