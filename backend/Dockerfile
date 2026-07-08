FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml mvnw ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
