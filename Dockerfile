# Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests clean package

# Run the packaged application
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/bug-tracker-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
