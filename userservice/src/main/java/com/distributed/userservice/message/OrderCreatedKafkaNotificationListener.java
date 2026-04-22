package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreatedKafkaNotificationListener {

    private final UserNotificationService userNotificationService;

    public OrderCreatedKafkaNotificationListener(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    @KafkaListener(
            topics = "${app.kafka.order-created.topic}",
            groupId = "${app.kafka.order-created.notification-group}"
    )
    public void handle(OrderCreatedEvent event) {
        userNotificationService.saveOrderCreatedNotification(event);
        log.info("Kafka 주문 생성 이벤트 소비 완료. orderId={}, userId={}", event.getOrderId(), event.getUserId());
    }
}
