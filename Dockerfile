# --- Stage 1: Build the application with Maven ---
FROM openjdk:17-jdk-slim AS build

WORKDIR /app

# Copy Maven Wrapper and pom.xml to cache dependencies
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the source code and build the application
COPY src ./src
RUN ./mvnw package -DskipTests -B

# --- Stage 2: Create the final, secure, and optimized production image ---
FROM eclipse-temurin:17-alpine

# Create a non-root user for security
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -s /bin/sh -D appuser

WORKDIR /app

# Copy the built jar from the 'build' stage
# Change this to match your notification service jar name
COPY --from=build /app/target/notification-service-*.jar notification-service.jar

# Change ownership to the non-root user
RUN chown appuser:appgroup notification-service.jar

# Switch to the non-root user
USER appuser

# Expose the port for notification service
EXPOSE 8085

# JVM optimization settings
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8085/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar notification-service.jar"]