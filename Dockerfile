FROM gradle:8.10.2-jdk17 AS build

WORKDIR /app

COPY --link build.gradle.kts settings.gradle.kts ./

COPY --link src ./src
COPY --link build.gradle.kts ./
COPY --link gradle.properties ./
COPY --link src/main/resources ./src/main/resources

RUN gradle build --no-daemon -x test

FROM openjdk:17-jdk-slim AS runtime

RUN apt-get update && apt-get install -y \
    firefox-esr \
    wget \
    && rm -rf /var/lib/apt/lists/*

RUN wget https://github.com/mozilla/geckodriver/releases/download/v0.35.0/geckodriver-v0.35.0-linux64.tar.gz \
    && tar -xvzf geckodriver-v0.35.0-linux64.tar.gz \
    && mv geckodriver /usr/bin/geckodriver \
    && chown root:root /usr/bin/geckodriver \
    && chmod +x /usr/bin/geckodriver \
    && rm geckodriver-v0.35.0-linux64.tar.gz

WORKDIR /app

COPY --from=build /app/build/libs/NaviDiscounts-*.jar /app/app.jar

COPY --from=build /app/src/main/resources/logback-spring.xml /app/
COPY --from=build /app/src/main/resources/banner.txt /app/

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_PORT=5432

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--webdriver.gecko.driver=/usr/bin/geckodriver"]