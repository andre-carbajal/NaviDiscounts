FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

COPY --link gradlew ./
COPY --link gradle ./gradle
COPY --link build.gradle.kts settings.gradle.kts gradle.properties ./
COPY --link src ./src

RUN chmod +x ./gradlew && ./gradlew build --no-daemon -x test

FROM eclipse-temurin:25-jre-alpine AS runtime

ARG TARGETARCH
ARG GECKODRIVER_VERSION=0.36.0

RUN apk add --no-cache \
    firefox \
    wget \
    tar

RUN set -eux; \
    case "${TARGETARCH}" in \
      amd64) gecko_arch="linux64" ;; \
      arm64) gecko_arch="linux-aarch64" ;; \
      *) echo "Unsupported architecture: ${TARGETARCH}" >&2; exit 1 ;; \
    esac; \
    wget "https://github.com/mozilla/geckodriver/releases/download/v${GECKODRIVER_VERSION}/geckodriver-v${GECKODRIVER_VERSION}-${gecko_arch}.tar.gz"; \
    tar -xvzf "geckodriver-v${GECKODRIVER_VERSION}-${gecko_arch}.tar.gz"; \
    mv geckodriver /usr/bin/geckodriver; \
    chown root:root /usr/bin/geckodriver; \
    chmod +x /usr/bin/geckodriver; \
    rm "geckodriver-v${GECKODRIVER_VERSION}-${gecko_arch}.tar.gz"

WORKDIR /app

COPY --from=build /app/build/libs/NaviDiscounts-*.jar /app/app.jar

COPY --from=build /app/src/main/resources/logback-spring.xml /app/
COPY --from=build /app/src/main/resources/banner.txt /app/

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_PORT=5432

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--webdriver.gecko.driver=/usr/bin/geckodriver"]
