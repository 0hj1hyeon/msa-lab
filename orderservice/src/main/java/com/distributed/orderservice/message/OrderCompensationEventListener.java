package com.distributed.orderservice.message;

import com.distributed.orderservice.dto.OrderCompensationRequestedEvent;
import com.distributed.orderservice.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCompensationEventListener {

    private final OrderService orderService;

    public OrderCompensationEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "${app.rabbitmq.order-compensation.queue}")
    public void handle(OrderCompensationRequestedEvent event) {
        orderService.compensateOrder(event);
        log.info("Saga 보상 이벤트 처리 완료. orderId={}, failedStep={}", event.getOrderId(), event.getFailedStep());
    }
}
