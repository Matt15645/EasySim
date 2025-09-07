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
 * 實現您論述中的架構：
 * 1. 停用 Session - 使服務無狀態
 * 2. JWT 過濾器 - 自動提取和驗證 JWT
 * 3. 設定安全上下文 - 自動創建 Authentication 物件
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * 核心安全配置 - 實現您論述中的所有要點
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // 1. 停用 Session - 實現無狀態設計（WebFlux 預設就是無狀態）
            
            // 2. 路由授權規則
            .authorizeExchange(exchanges -> exchanges
                // auth-service 的公開端點
                .pathMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                // 其他微服務需要 JWT 驗證
                .anyExchange().authenticated()
            )
            
            // 3. JWT 資源伺服器配置 - 自動處理 JWT 驗證
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
