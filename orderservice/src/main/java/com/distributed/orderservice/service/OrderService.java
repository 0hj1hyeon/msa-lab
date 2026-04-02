package com.distributed.orderservice.service;

import com.distributed.orderservice.domain.Order;
import com.distributed.orderservice.dto.OrderDto;
import com.distributed.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

        orderRepository.save(order);
        orderEventPublisher.publishOrderCreated(orderDto);

        return orderDto;
    }

    public Iterable<Order> getOrdersByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }
}
