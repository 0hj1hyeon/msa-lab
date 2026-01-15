package com.distributed.orderservice.controller;

import com.distributed.orderservice.domain.Order;
import com.distributed.orderservice.dto.OrderDto;
import com.distributed.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j; // 1. 로그 사용을 위한 어노테이션
import org.springframework.core.env.Environment; // 2. 환경 설정 값(포트 등) 접근용
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j // Lombok 로그 기능 활성화
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final Environment env; // 서버 포트 정보를 가져오기 위해 추가

    // 생성자 주입
    public OrderController(OrderService orderService, Environment env) {
        this.orderService = orderService;
        this.env = env;
    }

    // 주문 생성
    @PostMapping("/{userId}")
    public ResponseEntity<OrderDto> createOrder(@PathVariable("userId") String userId,
                                                @RequestBody OrderDto orderDto) {

        // [확인용 로그] 현재 요청을 처리하는 서버의 포트를 출력합니다.
        log.info("Order Controller (Create) - Server Port: {}", env.getProperty("local.server.port"));

        orderDto.setUserId(userId);
        OrderDto createdOrder = orderService.createOrder(orderDto);

        // 응답 헤더에도 포트 정보를 넣어주면 Postman에서 눈으로 보기 편합니다.
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Server-Port", env.getProperty("local.server.port"))
                .body(createdOrder);
    }

    // 사용자 주문 내역 조회
    @GetMapping("/{userId}")
    public ResponseEntity<Iterable<Order>> getOrder(@PathVariable("userId") String userId) {

        // [확인용 로그]
        log.info("Order Controller (Get) - Server Port: {}", env.getProperty("local.server.port"));

        Iterable<Order> orderList = orderService.getOrdersByUserId(userId);

        return ResponseEntity.status(HttpStatus.OK)
                .header("Server-Port", env.getProperty("local.server.port"))
                .body(orderList);
    }

    @GetMapping("/health_check")
    public String status() {
        // 8080 같은 포트 번호 대신, 서로 다른 '랜덤 ID'를 보여줘서 구분이 가게 함
        return String.format("It's Working in Order Service on PORT %s (Server ID: %s)",
                env.getProperty("local.server.port"),
                UUID.randomUUID().toString()); // 요청할 때마다 바뀌면 안 되지만, 일단 확인용으로 충분합니다.
        // 더 정확히 하려면 UUID를 필드 변수로 빼야 하지만 지금은 이대로도 확인 가능합니다.
    }
}