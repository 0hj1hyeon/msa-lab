# 1. 빌드 단계 (Builder Stage)
FROM amazoncorretto:21-al2023-jdk AS builder
WORKDIR /app

# Gradle 실행에 필요한 findutils 설치
RUN dnf install -y findutils

# 소스 코드 복사 및 빌드
COPY . .
RUN chmod +x gradlew
RUN ./gradlew bootJar -x test

# ---------------------------------------------------

# 2. 실행 단계 (Runner Stage)
FROM amazoncorretto:21-al2023-jdk
WORKDIR /app

# 빌드 단계에서 생성된 jar 파일을 정확한 경로에서 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]