package com.distributed.userservice.controller;

import com.distributed.userservice.dto.LoginRequest;
import com.distributed.userservice.dto.LoginResponse;
import com.distributed.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // ★ 여기가 중요합니다! 주소가 "/login" 인지 확인하세요.
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            String token = userService.authenticateAndGenerateToken(request.getUsername(), request.getPassword());

            LoginResponse response = LoginResponse.builder()
                    .success(true)
                    .message("로그인 성공")
                    .token(token)
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            LoginResponse errorResponse = LoginResponse.builder()
                    .success(false)
                    .message(e.getMessage()) // "User not found" or "Invalid password"
                    .token(null)
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }
}