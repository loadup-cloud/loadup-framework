# Build stage: compile and package with Maven + JDK 21
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy all POM files first for dependency caching (layer reuse when only sources change)
COPY pom.xml /app/
COPY loadup-dependencies/pom.xml /app/loadup-dependencies/
COPY commons/pom.xml /app/commons/
COPY components/pom.xml /app/components/
COPY middleware/pom.xml /app/middleware/
COPY modules/pom.xml /app/modules/
# Recursively copy all leaf POMs from multi-module directories
COPY commons/*/pom.xml /app/commons/
COPY components/*/pom.xml /app/components/
COPY components/*/*/pom.xml /app/components/
COPY components/*/*/*/pom.xml /app/components/
COPY middleware/*/pom.xml /app/middleware/
COPY middleware/*/*/pom.xml /app/middleware/
COPY middleware/*/*/*/pom.xml /app/middleware/
COPY modules/*/pom.xml /app/modules/
COPY modules/*/*/pom.xml /app/modules/
COPY loadup-application/pom.xml /app/loadup-application/

# Resolve dependencies (cached layer — invalidated only when POMs change)
RUN mvn dependency:go-offline -B -q -DskipTests

# Copy source code
COPY commons /app/commons/
COPY components /app/components/
COPY middleware /app/middleware/
COPY modules /app/modules/
COPY loadup-application /app/loadup-application/

# Build only the application module (pulls in all dependencies transitively)
RUN mvn clean package -pl loadup-application -am -B -q -DskipTests

# Runtime stage: minimal JRE 21 image
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S loadup && adduser -S loadup -G loadup

WORKDIR /app
COPY --from=builder /app/loadup-application/target/*.jar /app/app.jar

USER loadup

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseZGC", \
    "-XX:+ZGenerational", \
    "-Xmx512m", \
    "-Xms256m", \
    "-jar", "/app/app.jar"]
