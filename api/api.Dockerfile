FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# This copies the compiled JAR from your local 'target' dir into /app
COPY target/UniversalAPI.jar UniversalAPI.jar

EXPOSE 9504

ENTRYPOINT ["java", "-jar", "UniversalAPI.jar"]
