# Stage 1: Build
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

COPY --from=builder /app/target/erp-webapp-1.0.0.jar app.jar

# Render uses dynamic PORT
ENV PORT=8081

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]