FROM adoptopenjdk/openjdk11:latest
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} self-pm.jar
ENTRYPOINT ["java","-jar","/self-pm.jar"]
