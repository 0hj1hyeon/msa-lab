package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCreatedEvent;
import com.distributed.userservice.service.UserNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderNotificationEventListener {

    private final UserNotificationService userNotificationService;
    private final OrderNotificationFailureHandler orderNotificationFailureHandler;

    public OrderNotificationEventListener(UserNotificationService userNotificationService,
                                          OrderNotificationFailureHandler orderNotificationFailureHandler) {
        this.userNotificationService = userNotificationService;
        this.orderNotificationFailureHandler = orderNotificationFailureHandler;
    }

    @RabbitListener(queues = "${app.rabbitmq.order-created.notification-queue}")
    public void handle(OrderCreatedEvent event, Message message) {
        try {
            userNotificationService.saveOrderCreatedNotification(event);
            log.info("RabbitMQ 알림 큐 처리 완료. orderId={}, userId={}", event.getOrderId(), event.getUserId());
        } catch (Exception exception) {
            if (orderNotificationFailureHandler.shouldMoveToDlq(message)) {
                orderNotificationFailureHandler.publishToDlq(event, exception);
                return;
            }

            log.warn("RabbitMQ 알림 큐 처리 실패. retry 큐로 이동시킵니다. orderId={}, reason={}",
                    event.getOrderId(),
                    exception.getMessage());
            throw new AmqpRejectAndDontRequeueException("알림 처리 실패로 retry 큐로 이동", exception);
        }
    }
}
