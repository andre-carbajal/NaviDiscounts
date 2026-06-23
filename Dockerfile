FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

COPY --link gradlew ./
COPY --link gradle ./gradle
COPY --link build.gradle.kts settings.gradle.kts gradle.properties ./
COPY --link src ./src

RUN chmod +x ./gradlew && ./gradlew build --no-daemon -x test

FROM eclipse-temurin:25-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/NaviDiscounts-*.jar /app/app.jar

COPY --from=build /app/src/main/resources/logback-spring.xml /app/
COPY --from=build /app/src/main/resources/banner.txt /app/

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=dev

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
