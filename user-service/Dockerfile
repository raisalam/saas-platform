# Use Eclipse Temurin JDK 21 base image
FROM eclipse-temurin:21-jdk-jammy

# Set working directory inside container
WORKDIR /app

# Copy the Spring Boot JAR (built by Gradle)
COPY build/libs/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java","-jar","app.jar"]
