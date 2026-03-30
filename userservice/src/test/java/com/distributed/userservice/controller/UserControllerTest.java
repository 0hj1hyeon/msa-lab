package com.distributed.userservice.controller;

import com.distributed.userservice.dto.UserDto;
import com.distributed.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(com.distributed.userservice.config.SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void getUser_returnsUserDetails() throws Exception {
        UserDto response = new UserDto();
        response.setUserId("user-123");
        response.setUsername("alice");
        response.setEmail("alice@example.com");

        when(userService.getUserByUserId("user-123")).thenReturn(response);

        mockMvc.perform(get("/users/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void createUser_returnsCreatedUser() throws Exception {
        UserDto response = new UserDto();
        response.setUserId("user-123");
        response.setUsername("alice");

        when(userService.createUser(org.mockito.ArgumentMatchers.any(UserDto.class))).thenReturn(response);

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "email": "alice@example.com",
                                  "name": "Alice",
                                  "password": "plain-password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void healthCheck_returnsWorkingMessage() throws Exception {
        mockMvc.perform(get("/users/health_check"))
                .andExpect(status().isOk())
                .andExpect(content().string("It's Working in User Service"));

        verify(userService, org.mockito.Mockito.never()).getUserByUserId(org.mockito.ArgumentMatchers.anyString());
    }
}
