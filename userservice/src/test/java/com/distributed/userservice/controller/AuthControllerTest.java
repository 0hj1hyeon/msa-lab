package com.distributed.userservice.controller;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(com.distributed.userservice.config.SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @BeforeEach
    void printTestName(TestInfo testInfo) {
        System.out.println("[TEST] " + testInfo.getDisplayName());
    }

    @Test
    void login_returnsTokenWhenAuthenticationSucceeds() throws Exception {
        when(userService.authenticateAndGenerateToken("alice", "plain-password")).thenReturn("jwt-token");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "plain-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_returnsUnauthorizedWhenAuthenticationFails() throws Exception {
        when(userService.authenticateAndGenerateToken("alice", "wrong-password"))
                .thenThrow(new RuntimeException("Invalid password."));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "alice",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid password."))
                .andExpect(jsonPath("$.token").doesNotExist());
    }
}
