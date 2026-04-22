# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml ./
COPY libs/seetaface6JNI-1.0.0.jar ./libs/seetaface6JNI-1.0.0.jar
RUN mvn -q install:install-file \
    -Dfile=./libs/seetaface6JNI-1.0.0.jar \
    -DgroupId=com.cnsugar.ai \
    -DartifactId=seetaface6JNI \
    -Dversion=1.0.0 \
    -Dpackaging=jar
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

ENV TZ=Asia/Shanghai \
    LANG=C.UTF-8 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m"

COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
