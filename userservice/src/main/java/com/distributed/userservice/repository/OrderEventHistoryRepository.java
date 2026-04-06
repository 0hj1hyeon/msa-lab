package com.distributed.userservice.repository;

import com.distributed.userservice.domain.OrderEventHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderEventHistoryRepository extends JpaRepository<OrderEventHistory, Long> {
    List<OrderEventHistory> findByUserIdOrderByReceivedAtDesc(String userId);
}
