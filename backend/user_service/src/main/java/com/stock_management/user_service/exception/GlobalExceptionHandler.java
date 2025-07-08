package com.stock_management.user_service.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice  // 全局控制器增強，用於處理所有控制器拋出的異常
public class GlobalExceptionHandler {

    // 處理 AppException
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, String>> handleAppException(AppException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", ex.getStatus().toString());
        
        // 返回帶有錯誤信息和適當 HTTP 狀態碼的響應
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }
    
    // 處理所有其他未捕獲的異常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "An unexpected error occurred");
        errorResponse.put("details", ex.getMessage());
        
        // 返回 500 Internal Server Error
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
