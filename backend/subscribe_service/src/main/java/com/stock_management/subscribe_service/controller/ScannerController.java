package com.stock_management.subscribe_service.controller;

import com.stock_management.subscribe_service.dto.ScannerRequest;
import com.stock_management.subscribe_service.dto.ScannerResponse;
import com.stock_management.subscribe_service.service.ScannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scanner")
@RequiredArgsConstructor
@Slf4j
public class ScannerController {
    
    private final ScannerService scannerService;
    
    @PostMapping("/data")
    public ResponseEntity<ScannerResponse> getScannerData(@RequestBody ScannerRequest request) {
        try {
            log.info("收到掃描器資料請求: 類型={}, 日期={}, 數量={}", 
                     request.getScannerType(), request.getDate(), request.getCount());
            
            // 簡單驗證
            if (request.getScannerType() == null || request.getScannerType().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getDate() == null || request.getDate().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getCount() <= 0 || request.getCount() > 200) {
                return ResponseEntity.badRequest().build();
            }
            
            ScannerResponse response = scannerService.getScannerData(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("處理掃描器請求時發生錯誤: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
