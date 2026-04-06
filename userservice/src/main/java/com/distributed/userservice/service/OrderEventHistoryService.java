package com.distributed.userservice.service;

import com.distributed.userservice.domain.OrderEventHistory;
import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.dto.OrderEventHistoryDto;
import com.distributed.userservice.repository.OrderEventHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderEventHistoryService {

    private final OrderEventHistoryRepository orderEventHistoryRepository;

    public OrderEventHistoryService(OrderEventHistoryRepository orderEventHistoryRepository) {
        this.orderEventHistoryRepository = orderEventHistoryRepository;
    }

    public void saveOrderCreatedEvent(OrderCreatedEvent event) {
        OrderEventHistory orderEventHistory = new OrderEventHistory();
        orderEventHistory.setOrderId(event.getOrderId());
        orderEventHistory.setUserId(event.getUserId());
        orderEventHistory.setProductId(event.getProductId());
        orderEventHistory.setQty(event.getQty());
        orderEventHistory.setUnitPrice(event.getUnitPrice());
        orderEventHistory.setTotalPrice(event.getTotalPrice());
        orderEventHistory.setReceivedAt(LocalDateTime.now());

        orderEventHistoryRepository.save(orderEventHistory);
    }

    @Transactional(readOnly = true)
    public List<OrderEventHistoryDto> getOrderEventHistory(String userId) {
        return orderEventHistoryRepository.findByUserIdOrderByReceivedAtDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private OrderEventHistoryDto toDto(OrderEventHistory orderEventHistory) {
        return OrderEventHistoryDto.builder()
                .orderId(orderEventHistory.getOrderId())
                .userId(orderEventHistory.getUserId())
                .productId(orderEventHistory.getProductId())
                .qty(orderEventHistory.getQty())
                .unitPrice(orderEventHistory.getUnitPrice())
                .totalPrice(orderEventHistory.getTotalPrice())
                .receivedAt(orderEventHistory.getReceivedAt())
                .build();
    }
}
