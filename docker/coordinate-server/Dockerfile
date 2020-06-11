FROM adoptopenjdk:8-jdk-hotspot AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY CM CM
COPY common common
COPY coordinate-server coordinate-server
RUN chmod +x ./gradlew
RUN ./gradlew :coordinate-server:jar


FROM adoptopenjdk:8-jre-hotspot
WORKDIR /app
COPY --from=builder /app/coordinate-server/build/libs/coordinate-server-0.0.1.jar app.jar
COPY --from=builder /app/coordinate-server/cm-server.conf .
COPY --from=builder /app/coordinate-server/cm-session1.conf .
EXPOSE 7777
ENTRYPOINT ["java", "-jar", "app.jar"]
