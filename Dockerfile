# syntax=docker/dockerfile:experimental
FROM --platform=linux/amd64 eclipse-temurin:21.0.1_12-jdk-alpine AS build
WORKDIR /workspace/app
RUN apk add git
COPY . /workspace/app
COPY src/main/resources/META-INF /workspace/app/META-INF
RUN --mount=type=cache,target=/root/.gradle ./gradlew clean build -x test

FROM --platform=linux/amd64 eclipse-temurin:21.0.1_12-jre-alpine
WORKDIR /app/libs
COPY --from=build /workspace/app/build/libs/* /app/libs/
COPY --from=build /workspace/app/META-INF /app/META-INF

ENTRYPOINT ["java","-jar", "oauth-0.0.1-SNAPSHOT.jar"]