# msa-lab

Spring Boot와 Spring Cloud를 기반으로 구성한 MSA 학습 프로젝트입니다.  
이 프로젝트는 단순한 서비스 분리에서 끝나지 않고, `기본적인 MSA 구성 -> 서비스 간 통신 -> 이벤트 기반 처리 -> 장애 대응(Retry/DLQ) -> 보상 흐름(Saga) -> 메시징 시스템 비교(RabbitMQ / Kafka)` 순서로 확장해가며 전체 백엔드 흐름을 학습하는 것을 목표로 했습니다.

## 프로젝트 목표
- MSA 기본 구성 요소를 직접 조합해보기
- 서비스 간 동기 통신과 비동기 통신을 모두 경험해보기
- 메시지 브로커의 장애 대응 방식(Retry/DLQ)을 학습해보기
- 이벤트 기반 Saga 흐름을 단순한 구조로 이해해보기
- RabbitMQ와 Kafka를 같은 프로젝트 안에서 비교해보기

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

## 프로젝트 흐름
이 프로젝트는 아래 순서로 기능과 구조를 확장하며 발전했습니다.

1. `Spring Cloud 기반 MSA 기본 구조`
   Gateway, Eureka, Config Server를 먼저 구성해 서비스 분리와 중앙 설정 관리 흐름을 만들었습니다.
2. `서비스 간 동기 통신`
   `userservice`가 `orderservice`를 OpenFeign으로 호출해 사용자 조회와 주문 조회를 연결했습니다.
3. `이벤트 기반 비동기 처리`
   주문 생성 이후 후속 처리를 분리하기 위해 `OrderCreatedEvent`를 발행하고, `userservice`가 이를 소비하도록 구성했습니다.
4. `장애 대응과 실패 제어`
   notification 처리에는 Retry / DLQ와 예외 분류를 적용해 메시지 처리 실패를 제어했습니다.
5. `보상 흐름 학습`
   notification 최종 실패 시 보상 이벤트를 발행하고, `orderservice`가 주문 상태를 변경하는 학습용 Saga 흐름을 구성했습니다.
6. `메시징 시스템 비교`
   같은 `OrderCreatedEvent`를 Kafka로도 발행/소비하게 만들어 RabbitMQ와 Kafka 구조를 같은 프로젝트 안에서 비교할 수 있게 했습니다.

## 구현한 핵심 기능
### 1. MSA 기본 구조 구성
- Eureka 기반 서비스 등록/조회
- Config Server 기반 외부 설정 분리
- Docker 환경에서도 서비스 이름 기반 통신 가능하도록 구성

### 2. 서비스 간 동기 통신
- `userservice -> orderservice` OpenFeign 호출
- 사용자 조회 시 주문 목록을 함께 조합해서 반환
- 주문 서비스 장애 시 사용자 조회 전체가 깨지지 않도록 예외 처리 추가

### 3. 이벤트 기반 후속 처리
- `orderservice`에서 주문 생성 시 `OrderCreatedEvent` 발행
- `userservice`에서 history / notification / logging 큐로 역할 분리 소비
- 주문 이벤트 이력 저장
- 알림 저장
- 운영 로그 처리

### 4. 장애 대응 구조
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

### 6. 메시징 시스템 비교
- 기존 RabbitMQ 구조는 그대로 유지
- 같은 `OrderCreatedEvent`를 Kafka로도 발행
- `userservice`에서 Kafka consumer로 동일 이벤트 소비
- 같은 이벤트를 RabbitMQ와 Kafka에서 어떻게 다루는지 직접 비교 가능

### 7. 테스트와 실행 환경
- `userservice` 서비스/컨트롤러 테스트 추가
- 주문 서비스 장애 시 빈 목록 반환 테스트 추가
- RabbitMQ Retry/DLQ 테스트 추가
- Saga 보상 이벤트 발행/소비 테스트 추가
- Kafka producer / consumer 테스트 추가
- 외부 인프라 없이 컨텍스트가 뜨도록 스모크 테스트 정리

## 전체 흐름
### 1. 기본 요청 흐름
```text
Client
  -> API Gateway
  -> userservice / orderservice

userservice
  -> OpenFeign
  -> orderservice
  -> 사용자 정보 + 주문 목록 조합
```

### 2. 주문 생성 이후 후속 처리 흐름
```text
orderservice
  -> OrderCreatedEvent 발행
  -> RabbitMQ / Kafka

userservice
  -> 이벤트 소비
  -> history 저장
  -> notification 저장
  -> logging 처리
```

### 3. 실패 제어 흐름
```text
order.notification.queue
  -> 처리 실패
  -> order.notification.retry.queue
  -> TTL 대기
  -> 다시 order.notification.queue
  -> 최대 재시도 초과
  -> order.notification.dlq
```

### 4. 보상 흐름
```text
orderservice 주문 생성
  -> OrderCreatedEvent 발행
  -> userservice notification 처리
  -> 최종 실패(DLQ 대상)
  -> OrderCompensationRequestedEvent 발행
  -> orderservice가 보상 이벤트 소비
  -> 주문 상태 CREATED -> COMPENSATED
```

### 5. 비교 흐름
```text
orderservice
  -> RabbitMQ로 OrderCreatedEvent 발행
  -> Kafka로 OrderCreatedEvent 발행

userservice
  -> RabbitMQ listener 소비
  -> Kafka listener 소비
  -> 같은 알림 저장 로직 수행
```

## 메시징 비교 요약
- RabbitMQ는 `후속 처리 분리`, `Retry / DLQ`, `실패 제어`를 학습하는 데 집중했습니다.
- Kafka는 `같은 이벤트를 다른 방식으로 발행/소비하는 구조`를 비교하는 용도로 최소 적용했습니다.
- 이 프로젝트의 핵심은 특정 브로커 자체보다, `이벤트 흐름이 서비스 설계에 어떤 영향을 주는지`를 비교해보는 것입니다.

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

## 학습 포인트
### 구조를 단계적으로 확장하는 경험
- 처음부터 복잡한 구조를 한 번에 만들기보다, 기본 MSA 구성 위에 통신 방식과 장애 대응을 차례대로 얹었습니다.
- 프로젝트가 커질수록 서비스 책임, 이벤트 흐름, 상태 관리가 어떻게 달라지는지 직접 확인할 수 있었습니다.

### 동기 통신과 비동기 통신의 역할 분리
- 동기 호출은 즉시 응답이 필요한 조회 흐름에 사용했습니다.
- 비동기 이벤트는 주문 생성 이후 후속 처리와 책임 분리에 사용했습니다.

### 실패를 설계에 포함하는 경험
- 주문 서비스 장애 시 `userservice`는 빈 주문 목록으로 응답하도록 만들었습니다.
- notification 처리 실패에는 Retry / DLQ와 예외 분류를 적용해 장애를 단순 예외가 아니라 흐름으로 다뤘습니다.

### 상태 변경 중심으로 Saga 이해하기
- 새로운 서비스를 더 만들지 않고도, 최종 실패 -> 보상 이벤트 발행 -> 주문 상태 변경 흐름으로 Saga 개념을 학습할 수 있게 했습니다.

### 같은 흐름을 서로 다른 브로커로 비교하기
- RabbitMQ와 Kafka를 각각 기술 자체로 보기보다, 같은 `OrderCreatedEvent`가 두 구조에서 어떻게 처리되는지 비교하는 데 초점을 맞췄습니다.

## 앞으로 확장해볼 주제

* **Kafka Consumer Group 확장**

  * `order-created-topic`을 기반으로 history / logging consumer group 분리
  * 하나의 이벤트를 여러 서비스에서 병렬 소비하는 구조 설계

* **Kafka Retry / DLQ 및 재처리 전략 비교**

  * RabbitMQ의 Retry + DLQ 구조와 Kafka의 offset 기반 재처리 방식 비교
  * Dead Letter Topic(DLT) 설계 및 적용

* **공통 예외 처리 및 로그 표준화**

  * Custom Exception 구조 개선
  * Kafka / RabbitMQ 이벤트 처리 로그 포맷 통일
  * Correlation ID 기반 트래킹 구조 설계

* **모니터링 및 헬스 체크 강화**

  * Spring Boot Actuator 적용
  * 서비스 상태 및 메시지 처리 상태 모니터링
  * Kafka / RabbitMQ 메트릭 수집

* **아키텍처 다이어그램 시각화**

  * 전체 MSA 구조 (Gateway, Eureka, Config Server 포함)
  * RabbitMQ / Kafka / Saga 흐름 시각화

* **테스트 커버리지 확대**

  * Kafka Producer / Consumer 테스트 추가
  * Saga 보상 흐름 테스트
  * Retry / DLQ 시나리오 테스트


## 회고
이 프로젝트를 통해 단순 CRUD를 넘어서, 서비스를 분리한 이후 실제로 마주치는 흐름을 단계적으로 경험했습니다.

- 서비스 간 통신 방식 선택
- 설정 분리와 서비스 등록
- Docker 기반 실행 환경 구성
- 이벤트 기반 후속 처리
- 실패 제어와 보상 흐름
- 서로 다른 메시징 시스템 비교
- 테스트 가능한 코드 구조 만들기
- RabbitMQ 기반 비동기 처리와 Retry / DLQ
- 이벤트 기반 Saga 보상 흐름
- RabbitMQ와 Kafka 비교

현재는 `기본적인 MSA 구성 + 장애 대응 + 테스트 + 메시징 + Saga 개념 + Kafka 비교`까지 경험한 상태이며, 이후에는 운영성과 확장성을 더 강화하는 방향으로 발전시킬 계획입니다.
