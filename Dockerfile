# =========================================
# Stage 1: Build Frontend (Angular)
# =========================================
FROM node:20-alpine AS frontend-builder

WORKDIR /app/frontend

# Copy package files
COPY sygpress-app/package*.json ./

# Install dependencies
# Utilise --legacy-peer-deps pour résoudre les conflits de peer dependencies
RUN npm ci --legacy-peer-deps

# Copy frontend source
COPY sygpress-app/ ./

# Build Angular app for production
RUN npm run build

# =========================================
# Stage 2: Build Backend (Spring Boot)
# =========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-builder

WORKDIR /app/backend

# Copy pom.xml first for better caching
COPY sygpress-api/pom.xml ./
COPY sygpress-api/.mvn ./.mvn
COPY sygpress-api/mvnw ./

# Rendre mvnw exécutable
RUN chmod +x ./mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy backend source
COPY sygpress-api/src ./src

# Copy Angular build into Spring Boot static resources
COPY --from=frontend-builder /app/frontend/dist/sygpress-app/browser ./src/main/resources/static

# Build Spring Boot application
RUN ./mvnw clean package -DskipTests -B

# =========================================
# Stage 3: Runtime Image
# =========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Create uploads directory and set ownership
RUN mkdir -p /app/uploads && chown -R spring:spring /app

# Copy the built JAR from backend builder
COPY --from=backend-builder /app/backend/target/*.jar app.jar

# Change ownership of JAR file
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application with production profile
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
