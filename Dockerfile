# 1: Build (Using Gradle with JDK 25)
FROM gradle:jdk25 AS builder

# Set working directory
WORKDIR /app

# Copying dependencies first for caching, as they change less frequently
COPY build.gradle.kts settings.gradle.kts ./
# Downloading dependencies
RUN gradle clean build -x test --no-daemon || return 0

# Copying the rest of the source code
COPY src ./src
# Compiles into a fat jar
RUN gradle bootJar -x test --no-daemon

# 2: Run (Using Java 25 JRE)
# Using a lightweight JRE image for running the application
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copying the built jar from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]