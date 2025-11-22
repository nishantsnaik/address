# Use Temurin JDK 17
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the built jar
COPY target/address-0.0.1-SNAPSHOT.jar app.jar

# Run the Spring Boot app
ENTRYPOINT ["java","-jar","app.jar"]
