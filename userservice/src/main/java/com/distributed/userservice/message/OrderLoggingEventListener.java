package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderLoggingEventListener {

    @RabbitListener(queues = "${app.rabbitmq.order-created.logging-queue}")
    public void handle(OrderCreatedEvent event) {
        log.info("RabbitMQ 로깅 큐 처리 완료. orderId={}, userId={}, productId={}, totalPrice={}",
                event.getOrderId(),
                event.getUserId(),
                event.getProductId(),
                event.getTotalPrice());
    }
}
