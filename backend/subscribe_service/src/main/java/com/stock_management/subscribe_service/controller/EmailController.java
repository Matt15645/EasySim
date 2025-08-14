package com.stock_management.subscribe_service.controller;

import com.stock_management.subscribe_service.dto.EmailRequest;
import com.stock_management.subscribe_service.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@Slf4j
public class EmailController {
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        try {
            log.info("收到郵件發送請求，目標: {}", request.getTo());
            emailService.sendEmail(request);
            return ResponseEntity.ok("郵件發送成功至: " + request.getTo());
        } catch (Exception e) {
            log.error("郵件發送失敗: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("郵件發送失敗: " + e.getMessage());
        }
    }
    
    @PostMapping("/test")
    public ResponseEntity<String> sendTestEmail(@RequestParam String email) {
        try {
            log.info("收到測試郵件請求，目標: {}", email);
            emailService.sendTestEmail(email);
            return ResponseEntity.ok("測試郵件發送成功至: " + email);
        } catch (Exception e) {
            log.error("測試郵件發送失敗: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("測試郵件發送失敗: " + e.getMessage());
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Subscribe Service is running");
    }
}
