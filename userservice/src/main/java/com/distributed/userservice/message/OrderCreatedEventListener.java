package com.distributed.userservice.message;

import com.distributed.userservice.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCreatedEventListener {

    @RabbitListener(queues = "${app.rabbitmq.order-created.queue}")
    public void handle(OrderCreatedEvent event) {
        log.info("RabbitMQ 주문 생성 이벤트 수신 완료. orderId={}, userId={}, productId={}",
                event.getOrderId(),
                event.getUserId(),
                event.getProductId());
    }
}
