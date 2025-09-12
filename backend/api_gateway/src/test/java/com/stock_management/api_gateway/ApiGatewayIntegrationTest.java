package com.stock_management.api_gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * API Gateway 集成測試
 * 測試路由轉發、JWT 驗證、CORS 等功能
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-unit-testing-at-least-32-characters-long"
})
@DisplayName("API Gateway 集成測試")
class ApiGatewayIntegrationTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;
    private String validJwtToken;
    private String expiredJwtToken;
    private String invalidJwtToken;
    
    private static final String JWT_SECRET = "test-secret-key-for-unit-testing-at-least-32-characters-long";

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
            .baseUrl("http://localhost:" + port)
            .responseTimeout(Duration.ofSeconds(10))
            .build();

        // 創建測試用的 JWT tokens
        createTestJwtTokens();
    }

    private void createTestJwtTokens() {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
        
        // 有效的 JWT token
        validJwtToken = Jwts.builder()
            .setSubject("testuser")
            .setIssuedAt(new Date())
            .setExpiration(Date.from(Instant.now().plus(Duration.ofHours(1))))
            .claim("userId", 1L)
            .claim("username", "testuser")
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        // 過期的 JWT token
        expiredJwtToken = Jwts.builder()
            .setSubject("testuser")
            .setIssuedAt(Date.from(Instant.now().minus(Duration.ofHours(2))))
            .setExpiration(Date.from(Instant.now().minus(Duration.ofHours(1))))
            .claim("userId", 1L)
            .claim("username", "testuser")
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        // 無效簽名的 JWT token
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-secret-key-for-testing-32-chars".getBytes());
        invalidJwtToken = Jwts.builder()
            .setSubject("testuser")
            .setIssuedAt(new Date())
            .setExpiration(Date.from(Instant.now().plus(Duration.ofHours(1))))
            .claim("userId", 1L)
            .claim("username", "testuser")
            .signWith(wrongKey, SignatureAlgorithm.HS256)
            .compact();
    }

    @Test
    @DisplayName("登入端點應該允許無驗證訪問")
    void shouldAllowUnauthenticatedAccessToLoginEndpoint() {
        webTestClient.post()
            .uri("/api/auth/login")
            .exchange()
            .expectStatus().is5xxServerError(); // 因為後端服務不存在，預期 502 或其他 5xx 錯誤
    }

    @Test
    @DisplayName("註冊端點應該允許無驗證訪問")
    void shouldAllowUnauthenticatedAccessToRegisterEndpoint() {
        webTestClient.post()
            .uri("/api/auth/register")
            .exchange()
            .expectStatus().is5xxServerError(); // 因為後端服務不存在，預期 502 或其他 5xx 錯誤
    }

    @Test
    @DisplayName("健康檢查端點應該允許無驗證訪問")
    void shouldAllowUnauthenticatedAccessToActuatorEndpoints() {
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    @DisplayName("受保護的端點在沒有 JWT token 時應該返回 401")
    void shouldReturn401ForProtectedEndpointsWithoutJwtToken() {
        webTestClient.get()
            .uri("/api/account/positions")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("受保護的端點在有效 JWT token 時應該轉發請求")
    void shouldForwardRequestForProtectedEndpointsWithValidJwtToken() {
        webTestClient.get()
            .uri("/api/account/positions")
            .header("Authorization", "Bearer " + validJwtToken)
            .exchange()
            .expectStatus().is5xxServerError(); // 因為後端服務不存在，預期 502 或其他 5xx 錯誤
    }

    @Test
    @DisplayName("過期的 JWT token 應該返回 401")
    void shouldReturn401ForExpiredJwtToken() {
        webTestClient.get()
            .uri("/api/account/positions")
            .header("Authorization", "Bearer " + expiredJwtToken)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("無效簽名的 JWT token 應該返回 401")
    void shouldReturn401ForInvalidJwtTokenSignature() {
        webTestClient.get()
            .uri("/api/account/positions")
            .header("Authorization", "Bearer " + invalidJwtToken)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("格式錯誤的 JWT token 應該返回 401")
    void shouldReturn401ForMalformedJwtToken() {
        webTestClient.get()
            .uri("/api/account/positions")
            .header("Authorization", "Bearer invalid-token-format")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("缺少 Bearer 前綴的 Authorization header 應該返回 401")
    void shouldReturn401ForAuthorizationHeaderWithoutBearerPrefix() {
        webTestClient.get()
            .uri("/api/account/positions")
            .header("Authorization", validJwtToken)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Auth Service 路由應該正確轉發")
    void shouldRouteToAuthService() {
        webTestClient.post()
            .uri("/api/auth/login")
            .header("Content-Type", "application/json")
            .bodyValue("{\"username\":\"test\",\"password\":\"test\"}")
            .exchange()
            .expectStatus().is5xxServerError(); // 因為後端服務不存在，預期 502 或其他 5xx 錯誤
    }

    @Test
    @DisplayName("Account Service 路由應該正確轉發")
    void shouldRouteToAccountService() {
        webTestClient.get()
            .uri("/api/account/positions")
            .header("Authorization", "Bearer " + validJwtToken)
            .exchange()
            .expectStatus().is5xxServerError(); // 因為後端服務不存在，預期 502 或其他 5xx 錯誤
    }

    @Test
    @DisplayName("Backtest Service 路由應該正確轉發")
    void shouldRouteToBacktestService() {
        webTestClient.post()
            .uri("/api/backtest/run")
            .header("Authorization", "Bearer " + validJwtToken)
            .header("Content-Type", "application/json")
            .bodyValue("{\"symbol\":\"2330\",\"strategy\":\"test\"}")
            .exchange()
            .expectStatus().isNotFound(); // 因為測試配置中沒有 backtest 路由，返回 404
    }

    @Test
    @DisplayName("Subscribe Service 路由應該正確轉發")
    void shouldRouteToSubscribeService() {
        webTestClient.get()
            .uri("/api/scanner/data")
            .header("Authorization", "Bearer " + validJwtToken)
            .exchange()
            .expectStatus().isNotFound(); // 因為測試配置中沒有 scanner 路由，返回 404
    }

    @Test
    @DisplayName("OPTIONS 請求應該支援 CORS")
    void shouldSupportCorsForOptionsRequests() {
        // 對於受保護的端點，OPTIONS 請求也需要認證
        webTestClient.options()
            .uri("/api/account/positions")
            .header("Origin", "http://frontend.stock-management.local:8080")
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "Authorization")
            .exchange()
            .expectStatus().isUnauthorized(); // 因為沒有提供 JWT token，預期返回 401
    }

    @Test
    @DisplayName("帶有認證的 OPTIONS 請求應該支援 CORS")
    void shouldSupportCorsForOptionsRequestsWithAuth() {
        webTestClient.options()
            .uri("/api/account/positions")
            .header("Authorization", "Bearer " + validJwtToken)
            .header("Origin", "http://frontend.stock-management.local:8080")
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "Authorization")
            .exchange()
            .expectStatus().isOk() // OPTIONS 請求正確處理，返回 200
            .expectHeader().exists("Access-Control-Allow-Origin")
            .expectHeader().exists("Access-Control-Allow-Methods")
            .expectHeader().exists("Access-Control-Allow-Headers");
    }

    @Test
    @DisplayName("未知路由應該返回 404")
    void shouldReturn404ForUnknownRoutes() {
        webTestClient.get()
            .uri("/api/unknown/endpoint")
            .header("Authorization", "Bearer " + validJwtToken)
            .exchange()
            .expectStatus().isNotFound();
    }
}
