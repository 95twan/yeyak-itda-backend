# =================
# Runtime Stage
# =================
# 실제 애플리케이션을 실행할 경량 이미지를 만듭니다.
FROM openjdk:17-jdk-slim

# 작업 디렉토리를 생성합니다.
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일의 경로를 변수로 지정합니다.
ARG JAR_FILE=build/libs/*.jar

# 빌드 스테이지에서 만들어진 JAR 파일만 복사해오고, 이름을 app.jar로 변경합니다.
COPY ${JAR_FILE} app.jar

# 애플리케이션이 사용할 포트를 외부에 노출합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 실행할 명령어를 정의합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
