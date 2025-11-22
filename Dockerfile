# Build stage
FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root user
RUN addgroup --system appgroup && \
    adduser --system --no-create-home --group appuser && \
    chown -R appuser:appgroup /app

# Copy the built jar from builder
COPY --from=builder /app/target/address-*.jar app.jar

# Switch to non-root user
USER appuser

# Set JVM options
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the Spring Boot app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
