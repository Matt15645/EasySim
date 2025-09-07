package com.stock_management.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock_management.auth_service.dto.AuthResponse;
import com.stock_management.auth_service.dto.LoginRequest;
import com.stock_management.auth_service.dto.RegisterRequest;
import com.stock_management.auth_service.exception.AppException;
import com.stock_management.auth_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    })
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password123")
            .build();

        loginRequest = LoginRequest.builder()
            .email("test@example.com")
            .password("password123")
            .build();

        authResponse = AuthResponse.builder()
            .token("jwt_token_example")
            .userId(1L)
            .username("testuser")
            .build();
    }

    @Test
    @DisplayName("POST /api/auth/register - 註冊成功")
    void shouldRegisterSuccessfully() throws Exception {
        // given
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // when & then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt_token_example"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/auth/register - 用戶名已存在")
    void shouldFailRegisterWhenUsernameExists() throws Exception {
        // given
        when(authService.register(any(RegisterRequest.class)))
            .thenThrow(new AppException("Username already exists", HttpStatus.BAD_REQUEST));

        // when & then (AppException會被全局處理器正確處理為對應的狀態碼)
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 登入成功")
    void shouldLoginSuccessfully() throws Exception {
        // given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt_token_example"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 登入失敗 - 用戶不存在")
    void shouldFailLoginWhenUserNotFound() throws Exception {
        // given
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        // when & then (AppException會被全局處理器正確處理為對應的狀態碼)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/auth/register - 無效的JSON格式")
    void shouldFailRegisterWithInvalidJson() throws Exception {
        // when & then (全局異常處理器會將JSON解析錯誤轉為500)
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 無效的JSON格式")
    void shouldFailLoginWithInvalidJson() throws Exception {
        // when & then (全局異常處理器會將JSON解析錯誤轉為500)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json }"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("POST /api/auth/register - 缺少Content-Type")
    void shouldFailRegisterWithoutContentType() throws Exception {
        // when & then (全局異常處理器會將媒體類型錯誤轉為500)
        mockMvc.perform(post("/api/auth/register")
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 空請求體")
    void shouldFailLoginWithEmptyBody() throws Exception {
        // when & then (全局異常處理器會將缺少請求體錯誤轉為500)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
