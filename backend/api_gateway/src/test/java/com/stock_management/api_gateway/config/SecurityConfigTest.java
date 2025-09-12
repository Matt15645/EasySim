package com.stock_management.api_gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityConfig 單元測試
 * 測試 API Gateway 的安全配置和 JWT 解碼器
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = SecurityConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-unit-testing-at-least-32-characters-long"
})
@DisplayName("SecurityConfig 測試")
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Test
    @DisplayName("SecurityConfig Bean 應該正確創建")
    void shouldCreateSecurityConfigBean() {
        assertThat(securityConfig).isNotNull();
    }

    @Test
    @DisplayName("JWT 解碼器應該正確創建")
    void shouldCreateJwtDecoder() {
        // Given & When
        ReactiveJwtDecoder jwtDecoder = securityConfig.jwtDecoder();
        
        // Then
        assertThat(jwtDecoder).isNotNull();
        assertThat(jwtDecoder).isInstanceOf(ReactiveJwtDecoder.class);
    }

    @Test
    @DisplayName("安全過濾鏈應該正確創建")
    void shouldCreateSecurityWebFilterChain() {
        // Given & When
        SecurityWebFilterChain filterChain = securityConfig.securityWebFilterChain(
            org.springframework.security.config.web.server.ServerHttpSecurity.http()
        );
        
        // Then
        assertThat(filterChain).isNotNull();
    }
}
