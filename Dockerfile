# =================
# 1. Build Stage
# =================
# Gradle과 Java 17을 사용하여 애플리케이션을 빌드합니다.
FROM gradle:8.8.0-jdk17 AS builder

# 작업 디렉토리를 생성합니다.
WORKDIR /app

RUN apt-get update && apt-get install -y \
    procps \
    && rm -rf /var/lib/apt/lists/*

# 전체 프로젝트 파일을 컨테이너 안으로 복사합니다.
COPY . .

# gradlew 파일에 실행 권한을 부여합니다.
RUN chmod +x ./gradlew

ENV DE_FLAPDOODLE_MONGODB_EMBEDDED_VERSION=6.0.8

# Gradle을 사용하여 프로젝트를 빌드합니다. 테스트도 함께 실행됩니다.
#RUN ./gradlew build

RUN ./gradlew build -x test

# =================
# 2. Runtime Stage
# =================
# 실제 애플리케이션을 실행할 경량 이미지를 만듭니다.
FROM openjdk:17-jdk-slim

# 작업 디렉토리를 생성합니다.
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일의 경로를 변수로 지정합니다.
ARG JAR_FILE=build/libs/*.jar

# 빌드 스테이지에서 만들어진 JAR 파일만 복사해오고, 이름을 app.jar로 변경합니다.
COPY --from=builder /app/${JAR_FILE} app.jar

# 애플리케이션이 사용할 포트를 외부에 노출합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 실행할 명령어를 정의합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
