package com.stock_management.auth_service.service;

import com.stock_management.auth_service.dto.AuthResponse;
import com.stock_management.auth_service.dto.LoginRequest;
import com.stock_management.auth_service.dto.RegisterRequest;
import com.stock_management.auth_service.exception.AppException;
import com.stock_management.auth_service.model.User;
import com.stock_management.auth_service.repository.UserRepository;
import com.stock_management.auth_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service  // 標記為服務類
@RequiredArgsConstructor  // 生成帶有必需參數的構造函數，實現依賴注入
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;  // 用戶存儲庫
    private final PasswordEncoder passwordEncoder;  // 密碼編碼器
    private final JwtUtil jwtUtil;  // JWT 工具類
    
    @Override
    public AuthResponse register(RegisterRequest request) {
        // 檢查用戶名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Username already exists", HttpStatus.BAD_REQUEST);
        }
        
        // 檢查電子郵件是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already exists", HttpStatus.BAD_REQUEST);
        }
        
        // 創建用戶實體
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // 加密密碼
                .build();
        
        // 保存用戶到資料庫
        User savedUser = userRepository.save(user);
        
        // 創建 JWT
        String token = jwtUtil.generateToken(savedUser.getId());
        
        // 返回認證響應
        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // 根據電子郵件查找用戶
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED));
        
        // 驗證密碼
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
        
        // 創建 JWT
        String token = jwtUtil.generateToken(user.getId());
        
        // 返回認證響應
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }
}
