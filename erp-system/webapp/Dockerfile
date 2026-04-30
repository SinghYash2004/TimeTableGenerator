# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy jar from builder
COPY --from=builder /app/target/erp-webapp-1.0.0.jar app.jar

# Expose port
EXPOSE 8081

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]