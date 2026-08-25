# ---- build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Resolve dependencies in their own layer so source edits do not re-download
# the whole repository on every build.
COPY pom.xml ./
# Best-effort warm cache: failures here are ignored because the package step
# below downloads anything still missing.
RUN mvn -B -q dependency:go-offline || true

COPY src ./src

# Checkstyle's config lives outside this build context and only runs at the
# verify phase, so package skips it. Tests need a live MySQL and are run in CI,
# not in the image build.
RUN mvn -B clean package -DskipTests -Dcheckstyle.skip=true

# ---- runtime stage ---------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

# curl is used by the compose healthcheck
RUN apk add --no-cache curl \
    && addgroup -S wolfcafe && adduser -S wolfcafe -G wolfcafe

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

USER wolfcafe

EXPOSE 8080

# Every setting the app reads has an environment override (see
# application.properties), so this image needs no rebuild to move hosts.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
