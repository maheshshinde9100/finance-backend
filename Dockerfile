# ---------------------------
# Stage 1: Build
# ---------------------------
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the Spring Boot app (skip tests for faster build)
RUN mvn clean package -DskipTests

# ---------------------------
# Stage 2: Runtime
# ---------------------------
FROM eclipse-temurin:17-jre-focal

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port your app listens on
ENV SERVER_PORT=8081
EXPOSE 8081

# Health check for Render
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]