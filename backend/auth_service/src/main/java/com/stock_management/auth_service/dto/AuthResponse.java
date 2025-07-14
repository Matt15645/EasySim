package com.stock_management.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;     // 認證令牌 (在完整實現中應該是 JWT)
    private Long userId;      // 用戶 ID
    private String username;  // 用戶名
}