FROM adoptopenjdk:8-jdk-hotspot AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY CM CM
COPY common common
COPY server server
RUN chmod +x ./gradlew
RUN ./gradlew :server:jar


FROM adoptopenjdk:8-jre-hotspot
WORKDIR /app
COPY --from=builder /app/server/build/libs/server-0.0.1.jar app.jar
COPY --from=builder /app/server/cm-client.conf .
COPY --from=builder /app/server/config.yaml .
EXPOSE 9814
CMD ["java", "-jar", "app.jar"]
