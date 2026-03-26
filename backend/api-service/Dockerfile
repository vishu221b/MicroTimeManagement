FROM arm64v8/openjdk:17
WORKDIR app
COPY ./target/*.jar MTM-Api-Service-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "MTM-Api-Service-0.0.1-SNAPSHOT.jar"]