# Use a Java 17 runtime as the base image
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the compiled JAR file from your target folder to the container
# Note: Ensure you run './mvnw clean package' before building the image
COPY target/urlshortener-0.0.1-SNAPSHOT.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]