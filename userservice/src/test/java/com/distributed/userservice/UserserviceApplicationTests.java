package com.distributed.userservice;

import com.distributed.userservice.client.OrderServiceClient;
import com.distributed.userservice.repository.UserRepository;
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
        "jwt.secret=ZmFrZS1qd3Qtc2VjcmV0LWtleS1mb3ItdGVzdHMtZmFrZS1qd3Qtc2VjcmV0"
})
class UserserviceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private OrderServiceClient orderServiceClient;

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.containsBean("userController")).isTrue();
        assertThat(applicationContext.containsBean("authController")).isTrue();
    }
}
