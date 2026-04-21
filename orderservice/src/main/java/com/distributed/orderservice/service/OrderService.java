package com.distributed.orderservice.service;

import com.distributed.orderservice.domain.Order;
import com.distributed.orderservice.domain.OrderStatus;
import com.distributed.orderservice.dto.OrderCompensationRequestedEvent;
import com.distributed.orderservice.dto.OrderDto;
import com.distributed.orderservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository, OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    public OrderDto createOrder(OrderDto orderDto) {
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());

        Order order = new Order();
        order.setOrderId(orderDto.getOrderId());
        order.setUserId(orderDto.getUserId());
        order.setProductId(orderDto.getProductId());
        order.setQty(orderDto.getQty());
        order.setUnitPrice(orderDto.getUnitPrice());
        order.setTotalPrice(orderDto.getTotalPrice());
        order.setStatus(OrderStatus.CREATED);

        orderRepository.save(order);
        orderEventPublisher.publishOrderCreated(orderDto);

        return orderDto;
    }

    public Iterable<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public void compensateOrder(OrderCompensationRequestedEvent event) {
        Order order = orderRepository.findByOrderId(event.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("보상 처리 대상 주문을 찾을 수 없습니다."));

        order.setStatus(OrderStatus.COMPENSATED);
        log.info("Saga 보상 트랜잭션 수행. orderId={}, failedStep={}, reason={}",
                event.getOrderId(),
                event.getFailedStep(),
                event.getReason());
    }
}
