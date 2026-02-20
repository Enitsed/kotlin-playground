FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew build --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
