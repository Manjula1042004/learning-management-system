# Multi-stage Dockerfile with forced clean dependency refresh
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copy pom.xml and force a complete clean of dependencies
COPY pom.xml .
RUN mvn -B dependency:purge-local-repository -DreResolve=false -DactTransitively=false && \
    rm -rf ~/.m2/repository && \
    mvn -B dependency:go-offline

# Copy source code and build with clean package
COPY src src
RUN mvn -B clean package -DskipTests -U

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Expose port and run
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]