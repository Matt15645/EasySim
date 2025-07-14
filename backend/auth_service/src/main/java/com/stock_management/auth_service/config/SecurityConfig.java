package com.stock_management.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // 標記為 Spring 的組態類別
public class SecurityConfig {

    @Bean // 將密碼加密器註冊為 Spring Bean，供全域使用
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 採用 BCrypt 演算法進行密碼雜湊
    }

    @Bean // 定義 Spring Security 的過濾鏈設定
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 關閉 CSRF 保護（前後端分離常見做法，否則 POST/PUT/DELETE 會被攔截）
            .csrf(csrf -> csrf.disable())
            // 設定 API 權限規則
            .authorizeHttpRequests(auth -> auth
                // 允許所有人（不需登入）存取註冊與登入 API
                .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                // 其他所有 API 都需要認證（登入）
                .anyRequest().authenticated()
            )
            // 關閉預設的表單登入頁面（前端自己處理登入 UI）
            .formLogin(form -> form.disable())
            // 關閉 HTTP Basic 認證（不使用瀏覽器彈窗帳密驗證）
            .httpBasic(basic -> basic.disable());
        // 回傳設定完成的 SecurityFilterChain
        return http.build();
    }
}
