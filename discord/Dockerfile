FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# This copies the compiled JAR from your local 'target' dir into /app
COPY target/UniversalDiscord.jar UniversalDiscord.jar

EXPOSE 9506

ENTRYPOINT ["java", "-jar", "UniversalDiscord.jar"]
