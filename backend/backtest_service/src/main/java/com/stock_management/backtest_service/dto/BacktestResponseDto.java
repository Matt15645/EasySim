package com.stock_management.backtest_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResponseDto {
    private List<StockSeriesData> series;  // 多支股票的時間序列資料
    private LocalDateTime timestamp;       // 資料產生時間
    private String message;                // 訊息（成功/錯誤）
}