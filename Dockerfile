# Étape 1 : Construction du projet
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Étape 2 : Exécution du JAR
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/target/front-patient-0.0.1-SNAPSHOT.jar front-patient.jar
EXPOSE 8080
CMD ["java", "-jar", "front-patient.jar"]
