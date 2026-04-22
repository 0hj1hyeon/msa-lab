package com.distributed.orderservice.service;

import com.distributed.orderservice.domain.Order;
import com.distributed.orderservice.domain.OrderStatus;
import com.distributed.orderservice.dto.OrderCompensationRequestedEvent;
import com.distributed.orderservice.dto.OrderDto;
import com.distributed.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private OrderKafkaProducer orderKafkaProducer;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void createOrder_savesOrderAndPublishesRabbitMqEvent() {
        OrderDto orderDto = new OrderDto();
        orderDto.setUserId("user-123");
        orderDto.setProductId("product-1");
        orderDto.setQty(2);
        orderDto.setUnitPrice(1500);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto createdOrder = orderService.createOrder(orderDto);

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(savedOrder.capture());
        verify(orderEventPublisher).publishOrderCreated(createdOrder);
        verify(orderKafkaProducer).publish(createdOrder);

        assertThat(createdOrder.getOrderId()).isNotBlank();
        assertThat(createdOrder.getTotalPrice()).isEqualTo(3000);
        assertThat(savedOrder.getValue().getOrderId()).isEqualTo(createdOrder.getOrderId());
        assertThat(savedOrder.getValue().getTotalPrice()).isEqualTo(3000);
        assertThat(savedOrder.getValue().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void compensateOrder_changesOrderStatusToCompensated() {
        Order order = new Order();
        order.setOrderId("order-1");
        order.setUserId("user-123");
        order.setStatus(OrderStatus.CREATED);

        OrderCompensationRequestedEvent event = OrderCompensationRequestedEvent.builder()
                .orderId("order-1")
                .userId("user-123")
                .reason("notification failed")
                .failedStep("NOTIFICATION")
                .failureType("RetryableNotificationException")
                .build();

        when(orderRepository.findByOrderId("order-1")).thenReturn(Optional.of(order));

        orderService.compensateOrder(event);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPENSATED);
    }
}
