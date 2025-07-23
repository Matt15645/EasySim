package com.stock_management.backtest_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDataPoint {
    private String date;           // 日期，格式: "2025-07-22"
    private BigDecimal closePrice; // 收盤價
    private Long timestamp;        // 時間戳記
}
