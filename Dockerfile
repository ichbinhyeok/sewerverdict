FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew && ./gradlew --no-daemon bootJar

FROM bellsoft/liberica-openjre-alpine:17

WORKDIR /app

ENV JAVA_OPTS="-XX:+UseSerialGC -Xms256m -Xmx384m -Xss512k" \
    APP_STORAGE_ROOT=/app/storage \
    APP_CONTACT_EMAIL=contact@sewerclarity.com

COPY --from=build /workspace/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
