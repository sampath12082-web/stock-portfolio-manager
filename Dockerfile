# Stage 1: Build React frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./frontend/
COPY backend/src ./backend/src
RUN cd frontend && npm ci
COPY frontend/ ./frontend/
RUN cd frontend && npm run build

# Stage 2: Build Spring Boot JAR
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app/backend
COPY backend/mvnw backend/pom.xml ./
COPY backend/.mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q
COPY --from=frontend-build /app/backend/src ./src
RUN ./mvnw clean package -DskipTests
# Show what was built so we can confirm the JAR name in logs
RUN ls -lh target/

# Stage 3: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
# Copy using find to handle any JAR name
COPY --from=backend-build /app/backend/target/*.war ./
RUN find /app -name "*.war" -exec mv {} /app/app.war \; && chown app:app /app/app.war
USER app
EXPOSE 8081
ENTRYPOINT ["java","-Xmx400m","-Xms200m",\
  "-XX:+UseContainerSupport",\
  "-Dspring.profiles.active=prod",\
  "-jar","/app/app.war"]
