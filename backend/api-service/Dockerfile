FROM arm64v8/openjdk:17
WORKDIR app
COPY ./target/*.jar mtm_app.jar
ENTRYPOINT ["java", "-jar", "mtm_app.jar"]