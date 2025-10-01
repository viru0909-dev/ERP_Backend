# Use a minimal and secure Java 17 Runtime Environment from Eclipse Temurin
FROM eclipse-temurin:17-jre-alpine

# Set the working directory inside the container to /app
WORKDIR /app

# Define a variable for the path to the JAR file.
# This looks for any .jar file inside the 'target' directory.
ARG JAR_FILE=target/*.jar

# Copy the JAR file from your computer into the container and name it 'application.jar'
COPY ${JAR_FILE} application.jar

# This is the command that will run when the container starts.
# It simply executes your Spring Boot application.
ENTRYPOINT ["java", "-jar", "application.jar"]