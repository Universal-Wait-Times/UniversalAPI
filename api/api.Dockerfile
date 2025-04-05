FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

#COPY target/UniversalAPI.jar UniversalAPI.jar

EXPOSE 9504

ENTRYPOINT ["java", "-jar", "target/UniversalAPI.jar"]
