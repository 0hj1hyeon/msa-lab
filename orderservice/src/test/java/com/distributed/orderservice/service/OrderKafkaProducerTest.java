package com.distributed.orderservice.service;

import com.distributed.orderservice.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, com.distributed.orderservice.dto.OrderCreatedEvent> kafkaTemplate;

    private OrderKafkaProducer orderKafkaProducer;

    @BeforeEach
    void setUp() {
        orderKafkaProducer = new OrderKafkaProducer(kafkaTemplate, "order-created-topic");
    }

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void publish_sendsOrderCreatedEventToKafkaTopic() {
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId("order-1");
        orderDto.setUserId("user-123");
        orderDto.setProductId("product-1");
        orderDto.setQty(2);
        orderDto.setUnitPrice(1000);
        orderDto.setTotalPrice(2000);

        ArgumentCaptor<com.distributed.orderservice.dto.OrderCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(com.distributed.orderservice.dto.OrderCreatedEvent.class);

        orderKafkaProducer.publish(orderDto);

        verify(kafkaTemplate).send(eq("order-created-topic"), eq("order-1"), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getOrderId()).isEqualTo("order-1");
        assertThat(eventCaptor.getValue().getUserId()).isEqualTo("user-123");
    }
}
