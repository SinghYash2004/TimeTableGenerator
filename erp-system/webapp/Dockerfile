# ─────────────────────────────────────────────
# Stage 1: Build stage
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy dependency descriptor files first for better layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the application, skipping tests for faster image builds
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2: Runtime stage
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/erp-webapp-1.0.0.jar app.jar

# Set ownership to non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Expose the default application port
EXPOSE 8081

# Use PORT env var if provided (Render compatibility), otherwise default to 8081
ENV PORT=8081

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
