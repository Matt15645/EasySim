package com.stock_management.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // 生成 getter, setter, equals, hashCode, toString 方法
@Builder  // 建造者模式
@AllArgsConstructor  // 生成包含所有參數的構造函數
@NoArgsConstructor  // 生成無參構造函數
public class RegisterRequest {
    private String username;  // 用戶名
    private String email;     // 電子郵件
    private String password;  // 密碼 (明文，將在服務層加密)
}