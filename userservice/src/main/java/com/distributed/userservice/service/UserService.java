package com.distributed.userservice.service;

import com.distributed.userservice.client.OrderServiceClient;
import com.distributed.userservice.domain.User;
import com.distributed.userservice.dto.ResponseOrder;
import com.distributed.userservice.dto.UserDto;
import com.distributed.userservice.repository.UserRepository;
import com.distributed.userservice.util.JwtTokenProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final OrderServiceClient orderServiceClient; // [추가] Feign Client 주입

    public UserService(UserRepository userRepository,
                       JwtTokenProvider tokenProvider,
                       PasswordEncoder passwordEncoder,
                       OrderServiceClient orderServiceClient) { // [추가] 생성자 주입
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.orderServiceClient = orderServiceClient;
    }

    // [추가] 특정 사용자 조회 (주문 내역 포함)
    @Transactional(readOnly = true)
    public UserDto getUserByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDto userDto = new UserDto();
        userDto.setEmail(user.getEmail());
        userDto.setName(user.getName());
        userDto.setUserId(user.getUserId());
        userDto.setUsername(user.getUsername());

        /* [핵심] Feign Client를 사용하여 주문 서비스 호출 */
        List<ResponseOrder> ordersList = orderServiceClient.getOrders(userId);
        userDto.setOrders(ordersList);

        return userDto;
    }

    // [추가] 회원가입
    @Transactional
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        user.setUserId(userDto.getUserId());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        userRepository.save(user);
        return userDto;
    }

    // [기존] 로그인
    @Transactional
    public String authenticateAndGenerateToken(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (passwordEncoder.matches(password, user.getPassword())) {
            return tokenProvider.createToken(username);
        } else {
            throw new RuntimeException("Invalid password.");
        }
    }
}