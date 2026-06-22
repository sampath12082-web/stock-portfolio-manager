# ── Stage 1: Build React frontend ────────────────────────────────────────────
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build
# Output: /app/frontend/dist

# ── Stage 2: Build Spring Boot backend ───────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app/backend
COPY backend/mvnw backend/pom.xml ./
COPY backend/.mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q
COPY backend/src ./src
# Inject built React dist into Spring Boot static resources
# Spring Boot will serve React from / and API from /api/*
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN ./mvnw clean package -DskipTests -q
# Output: /app/backend/target/*.jar

# ── Stage 3: Minimal runtime image ───────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=backend-build /app/backend/target/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8081
ENTRYPOINT ["java", \
  "-Xmx400m", \
  "-Xms200m", \
  "-XX:+UseContainerSupport", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]
