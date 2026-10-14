# Base image: Official lightweight Eclipse Temurin JRE for Java 21
FROM eclipse-temurin:21-jre-alpine

# Set working directory inside the container
WORKDIR /app

# Copy the built Spring Boot executable JAR from the build context
COPY backend/target/vectordb-1.0.0-SNAPSHOT.jar app.jar

# Document container port
EXPOSE 8080

# Exec-form entrypoint to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
