package com.stock_management.backtest_service.controller;

import com.stock_management.backtest_service.dto.BacktestRequestDto;
import com.stock_management.backtest_service.dto.BacktestResponseDto;
import com.stock_management.backtest_service.service.BacktestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backtest")
@RequiredArgsConstructor
@Slf4j
public class BacktestController {

    private final BacktestService backtestService;

    /**
     * 執行股票回測分析
     */
    @PostMapping("/analyze")
    public ResponseEntity<BacktestResponseDto> analyzeStocks(@RequestBody BacktestRequestDto request) {
        try {
            log.info("收到回測請求：股票代號 {}, 日期區間 {} 到 {}", 
                     request.getSymbols(), request.getStartDate(), request.getEndDate());
            
            // 驗證請求參數
            if (request.getSymbols() == null || request.getSymbols().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            if (request.getStartDate() == null || request.getEndDate() == null) {
                return ResponseEntity.badRequest().build();
            }

            BacktestResponseDto response = backtestService.performBacktest(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("處理回測請求時發生錯誤", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 健康檢查端點
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Backtest Service is running");
    }
}
