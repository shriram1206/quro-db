# Build stage using Maven and OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
# Downloader dependencies to cache them in Docker layer
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/quro-*.jar app.jar
EXPOSE 8081
# Run with performance-optimized JVM options
ENTRYPOINT ["java", "-XX:+UseG1GC", "-jar", "app.jar"]
