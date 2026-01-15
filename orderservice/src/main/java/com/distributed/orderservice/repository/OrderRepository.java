package com.distributed.orderservice.repository;

import com.distributed.orderservice.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Iterable<Order> findByUserId(String userId);
}