FROM openjdk:8-jdk-alpine

LABEL maintainer="kevin.lee@microfocus.com"

USER root

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 80 available to the world outside this container
EXPOSE 80

# The application's jar file
ARG JAR_FILE=target/SpringEightBall.jar

# Copy the application's jar to the container
COPY ${JAR_FILE} app.jar

# JAVA_OPTS to be passed in
ENV JAVA_OPTS="-Xmx512m -Xss256k"

# Run the jar file
ENTRYPOINT ["java","-jar","/app.jar"]
