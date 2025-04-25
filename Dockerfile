FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=build/libs/\*.jar
COPY ${JAR_FILE} trelloflowbot.jar
ENTRYPOINT ["java", "-jar", "trelloflowbot.jar"]