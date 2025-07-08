package com.stock_management.user_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration  // 標記為 Spring 配置類
public class SecurityConfig {

    @Bean  // 將返回的物件注冊為 Spring Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 使用 BCrypt 加密算法，安全的密碼雜湊演算法
    }
}
