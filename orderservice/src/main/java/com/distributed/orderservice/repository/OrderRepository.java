package com.distributed.orderservice.repository;

import com.distributed.orderservice.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Iterable<Order> findByUserId(String userId);
    Optional<Order> findByOrderId(String orderId);
}
