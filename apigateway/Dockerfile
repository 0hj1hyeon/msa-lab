# 1. 빌드 단계 (Builder Stage)
FROM amazoncorretto:21-al2023-jdk AS builder
WORKDIR /app

# [추가] Gradle 실행에 필요한 findutils(xargs 포함) 설치
RUN dnf install -y findutils

# 소스 코드 복사
COPY . .

# 실행 권한 부여 및 빌드 실행
RUN chmod +x gradlew
RUN ./gradlew bootJar -x test

# 2. 실행 단계 (Runner Stage)
FROM amazoncorretto:21-al2023-jdk
WORKDIR /app

# 빌드 단계에서 생성된 jar 파일만 가져옴
COPY apigateway/build/libs/*.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]