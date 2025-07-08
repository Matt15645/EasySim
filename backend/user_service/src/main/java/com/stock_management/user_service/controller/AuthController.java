package com.stock_management.user_service.controller;

import com.stock_management.user_service.dto.AuthResponse;
import com.stock_management.user_service.dto.LoginRequest;
import com.stock_management.user_service.dto.RegisterRequest;
import com.stock_management.user_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController  // RESTful Web 服務控制器
@RequestMapping("/api/auth")  // 基本 URL 路徑
@RequiredArgsConstructor  // 生成帶有必需參數的構造函數
public class AuthController {
    
    private final AuthService authService;  // 認證服務
    
    // 處理註冊請求
    @PostMapping("/register")  // POST /api/auth/register
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        // 呼叫服務層註冊方法，並返回 200 OK 狀態碼
        return ResponseEntity.ok(authService.register(request));
    }
    
    // 處理登入請求
    @PostMapping("/login")  // POST /api/auth/login
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        // 呼叫服務層登入方法，並返回 200 OK 狀態碼
        return ResponseEntity.ok(authService.login(request));
    }
}
