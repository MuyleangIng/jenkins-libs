FROM openjdk:17
ENV SPRING_PROFILES_ACTIVE=prod
## Copy the jar file from the build/libs directory to the Docker image
COPY ./build/libs/*-SNAPSHOT.jar automatex.jar
VOLUME /images
EXPOSE 8086
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}","automatex.jar"]
