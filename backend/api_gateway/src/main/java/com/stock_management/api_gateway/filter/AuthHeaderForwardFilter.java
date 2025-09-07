package com.stock_management.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class AuthHeaderForwardFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 檢查是否有Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                // 解析JWT token獲取userId
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                
                String userId = claims.getSubject();
                
                // 創建新的請求，添加必要的headers
                ServerHttpRequest.Builder builder = request.mutate();
                builder.header("Authorization", authHeader);
                builder.header("X-User-Id", userId);
                
                return chain.filter(exchange.mutate().request(builder.build()).build());
            } catch (Exception e) {
                // JWT解析失敗，仍然轉發Authorization header，讓下游服務處理
                ServerHttpRequest.Builder builder = request.mutate();
                builder.header("Authorization", authHeader);
                
                return chain.filter(exchange.mutate().request(builder.build()).build());
            }
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // 確保在其他過濾器之前執行
    }
}
