# msa-lab

Spring Cloud 기반으로 구성한 MSA 학습 프로젝트입니다.  
서비스 디스커버리, API Gateway, Config Server, 동기 통신(OpenFeign), 비동기 메시징(RabbitMQ), 테스트 보강, Docker 실행 환경까지 직접 구성하며 백엔드 실무 흐름을 학습하는 것을 목표로 했습니다.

## 프로젝트 목표
- MSA 기본 구성 요소를 직접 조합해보기
- 서비스 간 동기/비동기 통신 방식을 모두 경험해보기
- 설정 분리, 배포 흐름, 장애 대응, 테스트 작성까지 학습하기
- 백엔드 취업 준비용 포트폴리오 프로젝트로 발전시키기

## 기술 스택
- Java 21
- Spring Boot 3.3
- Spring Cloud
- Spring Cloud Gateway
- Eureka Discovery Server
- Spring Cloud Config Server
- OpenFeign
- RabbitMQ
- Spring Data JPA
- MySQL
- Docker / Docker Compose
- GitHub Actions
- JUnit 5 / Mockito / MockMvc

## 서비스 구성
- `discoveryservice`
  Eureka 서버
- `configservice`
  외부 Git 저장소 기반 설정 서버
- `apigateway`
  라우팅 및 API 진입점
- `userservice`
  회원가입, 로그인, 사용자 조회, 주문 이벤트 이력 조회
- `orderservice`
  주문 생성, 주문 조회, 주문 생성 이벤트 발행
- `rabbitmq`
  주문 생성 이벤트 비동기 메시지 브로커
- `mysqldb`
  서비스 데이터 저장용 MySQL

## 아키텍처 요약
1. 클라이언트 요청은 `API Gateway`를 통해 각 서비스로 전달됩니다.
2. 각 서비스는 `Eureka`에 등록되고 서비스 이름 기반으로 통신합니다.
3. 설정 값은 `Config Server`를 통해 외부 Git 저장소에서 가져옵니다.
4. `userservice`는 `OpenFeign`을 사용해 `orderservice`를 동기 호출합니다.
5. `orderservice`는 주문 생성 후 `RabbitMQ`로 `OrderCreatedEvent`를 발행합니다.
6. `userservice`는 해당 이벤트를 소비해 주문 이벤트 이력을 저장하고 조회 API로 제공합니다.

## 구현한 핵심 기능
### 1. 서비스 디스커버리와 중앙 설정 관리
- Eureka 기반 서비스 등록/조회
- Config Server 기반 외부 설정 분리
- Docker 환경에서도 서비스 이름 기반 통신 가능하도록 구성

### 2. 동기 통신
- `userservice -> orderservice` OpenFeign 호출
- 사용자 조회 시 주문 목록을 함께 조합해서 반환
- 주문 서비스 장애 시 사용자 조회 전체가 깨지지 않도록 예외 처리 추가

### 3. 비동기 통신
- `orderservice`에서 주문 생성 시 `OrderCreatedEvent` 발행
- `userservice`에서 RabbitMQ 이벤트 소비
- 소비한 이벤트를 `order_event_history` 테이블에 저장
- `/users/{userId}/order-events` API로 이벤트 이력 조회

### 4. 테스트 보강
- `userservice` 서비스/컨트롤러 테스트 추가
- 주문 서비스 장애 시 빈 목록 반환 테스트 추가
- `orderservice` 주문 생성 및 이벤트 발행 테스트 추가
- RabbitMQ 이벤트 저장/조회 테스트 추가
- 외부 인프라 없이 컨텍스트가 뜨도록 스모크 테스트 정리

### 5. 배포 및 실행 환경
- `docker-compose` 기반 로컬 실행 환경 구성
- GitHub Actions 기반 서비스별 빌드/배포 흐름 구성
- EC2 배포를 고려한 기본 설정 정리

## 디렉토리 구조
```text
msa-lab
├── apigateway
├── configservice
├── discoveryservice
├── orderservice
├── userservice
├── docker-compose.yml
└── .github/workflows
```

## 실행 방법
### 1. 환경 변수 준비
프로젝트 루트 기준으로 `.env` 파일을 준비합니다.

예시:
```env
MYSQL_ROOT_PASSWORD=your-root-password
MYSQL_DATABASE=my_database
DB_USERNAME=root
DB_PASSWORD=your-root-password
JWT_SECRET=base64-encoded-secret

CONFIG_GIT_URI=https://github.com/your-account/your-config-repo.git
CONFIG_GIT_LABEL=main
CONFIG_GIT_USERNAME=your-github-id
CONFIG_GIT_TOKEN=your-github-token

RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

### 2. Docker Compose 실행
```bash
docker compose up --build
```

### 3. 주요 포트
- API Gateway: `8000`
- Eureka: `8761`
- Config Server: `8888`
- RabbitMQ AMQP: `5672`
- RabbitMQ Management: `15672`
- MySQL: `3307`

## 테스트 실행 방법
### userservice
```bash
cd userservice
bash ./gradlew cleanTest test
```

### orderservice
```bash
cd orderservice
bash ./gradlew cleanTest test
```

## 학습 포인트
### 동기 통신과 비동기 통신을 함께 사용
- 동기 호출은 즉시 응답이 필요한 조회 흐름에 사용
- RabbitMQ는 주문 생성 이후 후속 처리 분리에 사용

### 장애 대응
- 주문 서비스 호출 실패 시 `userservice`는 빈 주문 목록으로 응답
- 다른 서비스 장애가 전체 API 장애로 전파되지 않도록 처리

### 이벤트 기반 확장성
- 주문 생성 이벤트를 별도 저장 모델로 분리
- 이후 알림, 통계, 재처리, DLQ 같은 기능으로 확장 가능한 구조

## 앞으로 확장해볼 주제
- RabbitMQ 재시도 및 Dead Letter Queue
- 공통 예외 처리 및 로그 표준화
- 모니터링/헬스체크 강화
- README 아키텍처 다이어그램 추가
- 테스트 커버리지 확대

## 회고
이 프로젝트를 통해 단순 CRUD를 넘어서 MSA에서 자주 등장하는 문제를 직접 다뤘습니다.

- 서비스 간 통신 방식 선택
- 설정 분리와 서비스 등록
- Docker 기반 실행 환경 구성
- 테스트 가능한 코드 구조 만들기
- 메시지 브로커를 이용한 비동기 처리

현재는 "기본적인 MSA 구성 + 장애 대응 + 테스트 + 메시징"까지 경험한 상태이며, 이후에는 운영성과 확장성을 더 강화하는 방향으로 발전시킬 계획입니다.
