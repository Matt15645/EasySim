package com.stock_management.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.spec.SecretKeySpec;

/**
 * API Gateway 安全配置
 * 1. 登入/註冊 → 不需要驗證，直接轉發到 auth-service
 * 2. 其他請求 → 驗證 JWT token，然後轉發到相應微服務
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // 路由授權規則
            .authorizeExchange(exchanges -> exchanges
                // 登入/註冊端點不需要驗證
                .pathMatchers("/api/auth/login", "/api/auth/register").permitAll()
                // 健康檢查端點不需要驗證
                .pathMatchers("/actuator/**").permitAll()
                // 其他所有請求都需要 JWT 驗證
                .anyExchange().authenticated()
            )
            
            // JWT 資源伺服器配置 - 自動驗證 JWT token
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            )
            
            // 停用 CSRF（微服務架構中不需要）
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .build();
    }

    /**
     * JWT 解碼器 - 驗證 JWT 簽名和有效性
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        SecretKeySpec key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }
}
