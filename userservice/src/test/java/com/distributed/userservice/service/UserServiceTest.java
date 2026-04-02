package com.distributed.userservice.service;

import com.distributed.userservice.client.OrderServiceClient;
import com.distributed.userservice.domain.User;
import com.distributed.userservice.dto.ResponseOrder;
import com.distributed.userservice.dto.UserDto;
import com.distributed.userservice.repository.UserRepository;
import com.distributed.userservice.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId("user-123");
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setName("Alice");
        user.setPassword("encoded-password");
    }

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void getUserByUserId_returnsUserWithOrders() {
        ResponseOrder order = new ResponseOrder();
        order.setOrderId("order-1");

        when(userRepository.findByUserId("user-123")).thenReturn(Optional.of(user));
        when(orderServiceClient.getOrders("user-123")).thenReturn(List.of(order));

        UserDto result = userService.getUserByUserId("user-123");

        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getOrders()).hasSize(1);
        assertThat(result.getOrders().get(0).getOrderId()).isEqualTo("order-1");
    }

    @Test
    void getUserByUserId_throwsWhenUserDoesNotExist() {
        when(userRepository.findByUserId("missing-user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUserId("missing-user"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void getUserByUserId_returnsUserWithEmptyOrdersWhenOrderServiceFails() {
        when(userRepository.findByUserId("user-123")).thenReturn(Optional.of(user));
        when(orderServiceClient.getOrders("user-123")).thenThrow(new RuntimeException("orderservice unavailable"));

        UserDto result = userService.getUserByUserId("user-123");

        assertThat(result.getUserId()).isEqualTo("user-123");
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getOrders()).isEmpty();
    }

    @Test
    void createUser_savesEncodedPassword() {
        UserDto request = new UserDto();
        request.setUsername("alice");
        request.setEmail("alice@example.com");
        request.setName("Alice");
        request.setPassword("plain-password");

        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.createUser(request);

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());

        assertThat(result.getUserId()).isNotBlank();
        assertThat(savedUser.getValue().getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getValue().getUserId()).isEqualTo(result.getUserId());
    }

    @Test
    void authenticateAndGenerateToken_returnsTokenWhenPasswordMatches() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(tokenProvider.createToken("alice")).thenReturn("jwt-token");

        String token = userService.authenticateAndGenerateToken("alice", "plain-password");

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void authenticateAndGenerateToken_throwsWhenPasswordDoesNotMatch() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> userService.authenticateAndGenerateToken("alice", "wrong-password"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid password.");
    }
}
