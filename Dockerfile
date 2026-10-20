# ==============================================================================
# Multi-Stage Dockerfile for Motor Insurance Claim Portal
# Production-ready, secure, unprivileged runtime with health checking
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build Application Artifact with Maven & JDK 21
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache Maven dependencies by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy application source code
COPY src ./src

# Compile and package production JAR
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2: Secure, Lightweight JRE 21 Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="DevOps Team <devops@motorclaim.portal>"
LABEL description="Motor Insurance Claim Portal - Production Container"
LABEL version="1.0.0"

WORKDIR /app

# Create unprivileged system group and user
RUN addgroup -S -g 1001 claimgroup && \
    adduser -S -u 1001 -G claimgroup claimuser && \
    mkdir -p /app/data && \
    chown -R claimuser:claimgroup /app

# Copy executable jar from builder stage
COPY --from=builder --chown=claimuser:claimgroup /build/target/claim-portal.jar /app/claim-portal.jar

# Expose HTTP application port
EXPOSE 8080

# Configure container health probe
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run container as unprivileged user
USER claimuser

# Environment parameters
ENV SERVER_PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Entrypoint to start the application
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar claim-portal.jar --server.port=$SERVER_PORT"]
