# Step 1: Use a base image with Java
FROM openjdk:8-jdk-alpine

# Step 2: Set working directory
WORKDIR /app

# Step 3: Copy the jar file into the container
COPY target/hello-devops-1.0-SNAPSHOT.jar app.jar 

# Step 4: Run the jar
CMD ["java", "-jar", "app.jar"]

