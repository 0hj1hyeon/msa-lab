FROM amazoncorretto:21-alpine
COPY build/libs/*.jar ConfigService.jar
ENTRYPOINT ["java", "-jar", "ConfigService.jar"]