FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/identity-service-*.jar identity-service.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "identity-service.jar"]