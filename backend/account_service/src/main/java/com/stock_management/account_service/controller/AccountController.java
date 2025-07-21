package com.stock_management.account_service.controller;

import com.stock_management.account_service.dto.PieChartResponseDto;
import com.stock_management.account_service.dto.PortfolioResponseDto;
import com.stock_management.account_service.dto.PortfolioSummaryDto;
import com.stock_management.account_service.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/portfolio/pie-chart")
    public ResponseEntity<PieChartResponseDto> getPortfolioPieChart() {
        try {
            PieChartResponseDto response = accountService.getPortfolioPieChart();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("取得持股圓餅圖失敗", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/portfolio")
    public ResponseEntity<PortfolioResponseDto> getPortfolio() {
        try {
            PortfolioResponseDto response = accountService.getPortfolio();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("取得持股資料失敗", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/portfolio/summary")
    public ResponseEntity<PortfolioSummaryDto> getPortfolioSummary() {
        try {
            PortfolioSummaryDto response = accountService.getPortfolioSummary();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("取得投資組合摘要失敗", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
