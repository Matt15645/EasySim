package com.stock_management.auth_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter  // 生成 getter 方法
public class AppException extends RuntimeException {
    
    private final HttpStatus status;  // HTTP 狀態碼
    
    public AppException(String message, HttpStatus status) {
        super(message);  // 設定異常訊息
        this.status = status;  // 設定 HTTP 狀態碼
    }
}
