package com.distributed.userservice.service;

import com.distributed.userservice.domain.OrderEventHistory;
import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.dto.OrderEventHistoryDto;
import com.distributed.userservice.repository.OrderEventHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventHistoryServiceTest {

    @Mock
    private OrderEventHistoryRepository orderEventHistoryRepository;

    @InjectMocks
    private OrderEventHistoryService orderEventHistoryService;

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void saveOrderCreatedEvent_savesEventHistory() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .productId("product-1")
                .qty(2)
                .unitPrice(1000)
                .totalPrice(2000)
                .build();

        orderEventHistoryService.saveOrderCreatedEvent(event);

        ArgumentCaptor<OrderEventHistory> captor = ArgumentCaptor.forClass(OrderEventHistory.class);
        verify(orderEventHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getOrderId()).isEqualTo("order-1");
        assertThat(captor.getValue().getUserId()).isEqualTo("user-123");
        assertThat(captor.getValue().getReceivedAt()).isNotNull();
    }

    @Test
    void getOrderEventHistory_returnsHistoryDtos() {
        OrderEventHistory history = new OrderEventHistory();
        history.setOrderId("order-1");
        history.setUserId("user-123");
        history.setProductId("product-1");
        history.setQty(2);
        history.setUnitPrice(1000);
        history.setTotalPrice(2000);
        history.setReceivedAt(LocalDateTime.of(2026, 4, 6, 12, 0));

        when(orderEventHistoryRepository.findByUserIdOrderByReceivedAtDesc("user-123"))
                .thenReturn(List.of(history));

        List<OrderEventHistoryDto> result = orderEventHistoryService.getOrderEventHistory("user-123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderId()).isEqualTo("order-1");
        assertThat(result.get(0).getTotalPrice()).isEqualTo(2000);
    }
}
