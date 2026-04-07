package com.distributed.userservice.controller;

import com.distributed.userservice.dto.OrderEventHistoryDto;
import com.distributed.userservice.dto.UserNotificationDto;
import com.distributed.userservice.dto.UserDto;
import com.distributed.userservice.service.OrderEventHistoryService;
import com.distributed.userservice.service.UserNotificationService;
import com.distributed.userservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final OrderEventHistoryService orderEventHistoryService;
    private final UserNotificationService userNotificationService;

    public UserController(UserService userService,
                          OrderEventHistoryService orderEventHistoryService,
                          UserNotificationService userNotificationService) {
        this.userService = userService;
        this.orderEventHistoryService = orderEventHistoryService;
        this.userNotificationService = userNotificationService;
    }

    @GetMapping("/health_check")
    public String status() {
        return "It's Working in User Service";
    }

    // [추가] 사용자 상세 정보 조회 (주문 내역 포함)
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable("userId") String userId) {
        UserDto userDto = userService.getUserByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(userDto);
    }

    @GetMapping("/{userId}/order-events")
    public ResponseEntity<List<OrderEventHistoryDto>> getOrderEventHistory(@PathVariable("userId") String userId) {
        List<OrderEventHistoryDto> history = orderEventHistoryService.getOrderEventHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{userId}/notifications")
    public ResponseEntity<List<UserNotificationDto>> getNotifications(@PathVariable("userId") String userId) {
        List<UserNotificationDto> notifications = userNotificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
