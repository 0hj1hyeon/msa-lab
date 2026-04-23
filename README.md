# msa-lab

Spring Boot와 Spring Cloud를 기반으로 구성한 MSA 학습 프로젝트입니다.  
서비스 디스커버리, API Gateway, Config Server, 동기 통신(OpenFeign), 비동기 메시징(RabbitMQ), Retry/DLQ, 학습용 Saga 보상 흐름, Kafka 비교 구조까지 직접 구성하며 백엔드 실무에서 자주 만나는 흐름을 단계적으로 학습하는 것을 목표로 했습니다.

## 프로젝트 목표
- MSA 기본 구성 요소를 직접 조합해보기
- 서비스 간 동기 통신과 비동기 통신을 모두 경험해보기
- 메시지 브로커의 장애 대응 방식(Retry/DLQ)을 학습해보기
- 이벤트 기반 Saga 흐름을 단순한 구조로 이해해보기
- RabbitMQ와 Kafka를 같은 프로젝트 안에서 비교해보기
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
- Apache Kafka
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
  회원가입, 로그인, 사용자 조회, 주문 이벤트 소비, 알림 저장, Retry/DLQ, 보상 이벤트 발행
- `orderservice`
  주문 생성, 주문 조회, 주문 생성 이벤트 발행, Saga 보상 이벤트 소비
- `rabbitmq`
  주문 생성 이벤트 비동기 메시지 브로커
- `kafka`
  같은 이벤트 흐름을 비교하기 위한 스트리밍 브로커
- `mysqldb`
  서비스 데이터 저장용 MySQL

## 아키텍처 요약
1. 클라이언트 요청은 `API Gateway`를 통해 각 서비스로 전달됩니다.
2. 각 서비스는 `Eureka`에 등록되고 서비스 이름 기반으로 통신합니다.
3. 설정 값은 `Config Server`를 통해 외부 Git 저장소에서 가져옵니다.
4. `userservice`는 `OpenFeign`을 사용해 `orderservice`를 동기 호출합니다.
5. `orderservice`는 주문 생성 후 같은 `OrderCreatedEvent`를 RabbitMQ와 Kafka로 각각 발행합니다.
6. `userservice`는 RabbitMQ와 Kafka에서 동일한 주문 생성 이벤트를 소비해 알림 저장 흐름을 비교할 수 있습니다.
7. RabbitMQ notification 큐는 Retry/DLQ를 통해 실패를 제어합니다.
8. notification 최종 실패 시 `userservice`는 Saga 보상 이벤트를 발행하고, `orderservice`는 이를 소비해 주문 상태를 변경합니다.

## 구현한 핵심 기능
### 1. 서비스 디스커버리와 중앙 설정 관리
- Eureka 기반 서비스 등록/조회
- Config Server 기반 외부 설정 분리
- Docker 환경에서도 서비스 이름 기반 통신 가능하도록 구성

### 2. 동기 통신
- `userservice -> orderservice` OpenFeign 호출
- 사용자 조회 시 주문 목록을 함께 조합해서 반환
- 주문 서비스 장애 시 사용자 조회 전체가 깨지지 않도록 예외 처리 추가

### 3. RabbitMQ 기반 이벤트 처리
- `orderservice`에서 주문 생성 시 `OrderCreatedEvent` 발행
- `userservice`에서 history / notification / logging 큐로 역할 분리 소비
- 주문 이벤트 이력 저장
- 알림 저장
- 운영 로그 처리

### 4. Retry / DLQ
- notification 큐에 Retry + DLQ 구조 적용
- Retry 가능한 예외와 불가능한 예외를 분리
- Retry 가능한 예외는 retry 큐 -> TTL -> 재처리 -> DLQ 흐름 유지
- Retry 불가능한 예외는 즉시 DLQ 이동
- 최종 실패 메시지에 에러 정보 헤더 기록

### 5. 학습용 Saga 보상 흐름
- 기존 서비스 구조는 유지하고, 이벤트 기반 Choreography 개념만 적용
- `userservice`에서 notification 최종 실패 시 `OrderCompensationRequestedEvent` 발행
- `orderservice`에서 보상 이벤트를 소비하여 주문 상태를 `CREATED -> COMPENSATED`로 변경
- 새 서비스를 추가하지 않고도 보상 트랜잭션 흐름을 코드로 확인 가능

### 6. Kafka 비교 구조
- 기존 RabbitMQ 구조는 그대로 유지
- 같은 `OrderCreatedEvent`를 Kafka로도 발행
- `userservice`에서 Kafka consumer로 동일 이벤트 소비
- 같은 이벤트를 RabbitMQ와 Kafka에서 어떻게 다루는지 직접 비교 가능

### 7. 테스트 보강
- `userservice` 서비스/컨트롤러 테스트 추가
- 주문 서비스 장애 시 빈 목록 반환 테스트 추가
- RabbitMQ Retry/DLQ 테스트 추가
- Saga 보상 이벤트 발행/소비 테스트 추가
- Kafka producer / consumer 테스트 추가
- 외부 인프라 없이 컨텍스트가 뜨도록 스모크 테스트 정리

## 이벤트 흐름
### RabbitMQ 기본 흐름
```text
orderservice
  -> OrderCreatedEvent 발행
  -> order.exchange / order.created

userservice
  -> history queue 소비
  -> notification queue 소비
  -> logging queue 소비
```

### RabbitMQ Retry / DLQ 흐름
```text
order.notification.queue
  -> 처리 실패
  -> order.notification.retry.queue
  -> TTL 대기
  -> 다시 order.notification.queue
  -> 최대 재시도 초과
  -> order.notification.dlq
```

### Saga 보상 흐름
```text
orderservice 주문 생성
  -> OrderCreatedEvent 발행
  -> userservice notification 처리
  -> 최종 실패(DLQ 대상)
  -> OrderCompensationRequestedEvent 발행
  -> orderservice가 보상 이벤트 소비
  -> 주문 상태 CREATED -> COMPENSATED
```

### Kafka 비교 흐름
```text
orderservice
  -> RabbitMQ로 OrderCreatedEvent 발행
  -> Kafka로 OrderCreatedEvent 발행

userservice
  -> RabbitMQ listener 소비
  -> Kafka listener 소비
  -> 같은 알림 저장 로직 수행
```

## RabbitMQ와 Kafka 비교
### RabbitMQ
- exchange, queue, routing key 중심 구조
- Retry / DLQ 설계가 직관적
- 작업 큐, 후속 처리 분리, 실패 제어에 강점

### Kafka
- topic, partition, consumer group 중심 구조
- 이벤트를 로그처럼 저장하고 여러 consumer group이 독립적으로 읽음
- 이벤트 스트리밍, 재처리, 확장성 비교에 강점

### 이 프로젝트에서의 비교 포인트
- 같은 `OrderCreatedEvent`를 두 메시징 시스템에서 모두 발행
- `userservice`에서 두 방식 모두 소비
- RabbitMQ는 queue 기반 소비 구조
- Kafka는 topic + consumer group 기반 소비 구조

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
- Kafka: `9092`
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
- RabbitMQ와 Kafka는 주문 생성 이후 후속 처리 분리에 사용

### 장애 대응
- 주문 서비스 호출 실패 시 `userservice`는 빈 주문 목록으로 응답
- RabbitMQ notification 큐는 Retry / DLQ로 장애를 제어
- 예외를 retryable / non-retryable로 나눠 불필요한 재시도를 줄임

### 이벤트 기반 확장성
- 주문 생성 이벤트를 history / notification / logging으로 분리 소비
- 동일한 이벤트를 Kafka로도 흘려 비교 가능
- 이후 통계, 모니터링, 재처리, 추가 consumer group 확장 가능

### Saga 개념 학습
- 중앙 오케스트레이터 없이 이벤트 기반 Choreography 방식으로 보상 흐름 구성
- 최종 실패 시 보상 이벤트를 발행하고 다른 서비스가 상태를 바꾸는 흐름 경험

## 앞으로 확장해볼 주제
- Kafka consumer group을 history / logging까지 확장
- Kafka Retry / DLQ 또는 재처리 전략 비교
- 공통 예외 처리 및 로그 표준화
- 모니터링 / 헬스체크 강화
- README 아키텍처 다이어그램 시각화
- 테스트 커버리지 확대

## 회고
이 프로젝트를 통해 단순 CRUD를 넘어서 MSA에서 자주 등장하는 문제를 직접 다뤘습니다.

- 서비스 간 통신 방식 선택
- 설정 분리와 서비스 등록
- Docker 기반 실행 환경 구성
- 테스트 가능한 코드 구조 만들기
- RabbitMQ 기반 비동기 처리와 Retry / DLQ
- 이벤트 기반 Saga 보상 흐름
- RabbitMQ와 Kafka 비교

현재는 `기본적인 MSA 구성 + 장애 대응 + 테스트 + 메시징 + Saga 개념 + Kafka 비교`까지 경험한 상태이며, 이후에는 운영성과 확장성을 더 강화하는 방향으로 발전시킬 계획입니다.
