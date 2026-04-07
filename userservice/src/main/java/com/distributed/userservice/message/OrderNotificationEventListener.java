package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderNotificationEventListener {

    private final UserNotificationService userNotificationService;

    public OrderNotificationEventListener(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    @RabbitListener(queues = "${app.rabbitmq.order-created.notification-queue}")
    public void handle(OrderCreatedEvent event) {
        userNotificationService.saveOrderCreatedNotification(event);
        log.info("RabbitMQ 알림 큐 처리 완료. orderId={}, userId={}", event.getOrderId(), event.getUserId());
    }
}
