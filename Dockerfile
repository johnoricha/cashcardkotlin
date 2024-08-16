FROM openjdk:17-jdk-slim

# create user to run app app (instead of root)
RUN groupadd -r app && useradd -r -g app app

# use user "app"
USER app

# copy the jar file into the docker image
COPY build/libs/*.jar app.jar

# run the jar file
ENTRYPOINT ["java", "-jar", "/app.jar"]