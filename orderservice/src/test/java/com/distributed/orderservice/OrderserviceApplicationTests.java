package com.distributed.orderservice;

import com.distributed.orderservice.repository.OrderRepository;
import com.distributed.orderservice.service.OrderEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.rabbitmq.listener.direct.auto-startup=false"
})
class OrderserviceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private OrderEventPublisher orderEventPublisher;

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.containsBean("orderController")).isTrue();
        assertThat(applicationContext.containsBean("orderService")).isTrue();
    }
}
