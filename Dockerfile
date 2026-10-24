# Stage 1: build the JAR (tests run in CI: see .github/workflows/ci.yml)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first to leverage Docker layer caching
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn -B clean package -DskipTests

# Stage 2: minimal runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/*.jar app.jar

# Render provides the port in $PORT (default 10000)
ENV PORT=10000
# Size the heap from the container limit (Render free tier = 512 MB) and die cleanly on OOM so the platform restarts us
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
EXPOSE 10000

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget -q -O /dev/null "http://localhost:${PORT}/api/auth/health" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_TOOL_OPTIONS $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
