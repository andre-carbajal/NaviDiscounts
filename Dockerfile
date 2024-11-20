FROM openjdk:17-jdk-slim

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

COPY . .

RUN chmod +x gradlew

RUN ./gradlew build -x test

COPY build/libs/*.jar /app/app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_HOST=db_host
ENV DB_PORT=5432
ENV DB_NAME=db_name
ENV DATASOURCE_USERNAME=db_username
ENV DATASOURCE_PASSWORD=db_password
ENV TELEGRAM_BOT_TOKEN=telegram_bot_token

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--webdriver.gecko.driver=/usr/bin/geckodriver"]