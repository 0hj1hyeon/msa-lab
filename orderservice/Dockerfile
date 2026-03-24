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

# ---------------------------------------------------

# 2. 실행 단계 (Runner Stage)
FROM amazoncorretto:21-al2023-jdk
WORKDIR /app

# [핵심 수정] 폴더명(orderservice 등)을 지우고, --from=builder와 절대 경로를 사용합니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 실행
ENTRYPOINT ["java", "-jar", "app.jar"]