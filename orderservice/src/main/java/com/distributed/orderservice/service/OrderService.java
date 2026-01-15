package com.distributed.orderservice.service;

import com.distributed.orderservice.domain.Order;
import com.distributed.orderservice.dto.OrderDto;
import com.distributed.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate; // 추가됨

import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    // RestTemplate: 다른 서버에 HTTP 요청을 보내는 도구
    private final RestTemplate restTemplate = new RestTemplate();

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderDto createOrder(OrderDto orderDto) {
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());

        // ---------------------------------------------------------
        // [추가된 부분] User Service 호출하여 "살아있니?" 찔러보기
        // ---------------------------------------------------------
        // docker-compose.yml에 적힌 서비스 이름(userservice)을 호스트로 사용
        String userUrl = String.format("http://userservice:8080/users/health_check");

        try {
            // User Service에게 GET 요청을 보냄
            String response = restTemplate.getForObject(userUrl, String.class);
            System.out.println("User Service 응답: " + response);
        } catch (Exception e) {
            System.err.println("User Service 호출 실패: " + e.getMessage());
            // 실험을 위해 에러가 나도 주문은 진행시킴
        }
        // ---------------------------------------------------------

        Order order = new Order();
        order.setOrderId(orderDto.getOrderId());
        order.setUserId(orderDto.getUserId());
        order.setProductId(orderDto.getProductId());
        order.setQty(orderDto.getQty());
        order.setUnitPrice(orderDto.getUnitPrice());
        order.setTotalPrice(orderDto.getTotalPrice());

        orderRepository.save(order);

        return orderDto;
    }

    public Iterable<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }
}