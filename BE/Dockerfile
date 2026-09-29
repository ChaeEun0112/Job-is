# 1단계: Gradle로 빌드
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# 2단계: 실행 이미지는 JRE만
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends python3 python3-pip \
    && rm -rf /var/lib/apt/lists/*
COPY database database
RUN pip3 install --no-cache-dir -r database/matching/requirements.txt
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
