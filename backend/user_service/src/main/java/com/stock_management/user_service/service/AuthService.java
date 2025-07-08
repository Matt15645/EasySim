package com.stock_management.user_service.service;

import com.stock_management.user_service.dto.AuthResponse;
import com.stock_management.user_service.dto.LoginRequest;
import com.stock_management.user_service.dto.RegisterRequest;

public interface AuthService {
    // 註冊方法: 接收註冊請求，返回認證響應
    AuthResponse register(RegisterRequest request);
    
    // 登入方法: 接收登入請求，返回認證響應
    AuthResponse login(LoginRequest request);
}
