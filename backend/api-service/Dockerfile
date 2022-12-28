FROM arm64v8/openjdk:17
WORKDIR app
ENV PORT 8080
EXPOSE 8080
COPY ./target/*.jar mtm_app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=prod", "-jar", "mtm_app.jar"]