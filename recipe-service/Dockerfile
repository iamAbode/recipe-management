FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/recipe-service-*.jar recipe-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "recipe-service.jar"]