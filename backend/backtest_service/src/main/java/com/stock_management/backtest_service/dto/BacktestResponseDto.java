package com.stock_management.backtest_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResponseDto {
    private BacktestResult result;         // 回測結果
    private List<PortfolioSnapshot> portfolioHistory; // 投資組合歷史
    private LocalDateTime timestamp;       // 資料產生時間
    private String message;                // 訊息（成功/錯誤）
}