# Use Amazon Corretto, which has excellent multi-platform support for Mac M1/M2/M3
FROM amazoncorretto:17-alpine-jdk

# Set the working directory inside the container to /app
WORKDIR /app

# Define a variable for the path to the JAR file.
ARG JAR_FILE=target/*.jar

# Copy the JAR file from your computer into the container and name it 'application.jar'
COPY ${JAR_FILE} application.jar

# This is the command that will run when the container starts.
ENTRYPOINT ["java", "-jar", "application.jar"]