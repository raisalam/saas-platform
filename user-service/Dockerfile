# Use Eclipse Temurin JDK 21 base image
FROM eclipse-temurin:21-jre

# Set working directory inside container
WORKDIR /app

# Copy the Spring Boot JAR (built by Gradle)
COPY build/libs/*.jar app.jar

ENV KAFKA_OPTS=""
ENV JAVA_TOOL_OPTIONS=""
ENV JAVA_OPTS=""

# Expose the port your app runs on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java","-jar","app.jar"]
