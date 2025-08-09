package com.stock_management.backtest_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSnapshot {
    private String date;                    // 日期
    private BigDecimal cash;                // 現金
    private Map<String, Integer> holdings;  // 持股 {股票代號: 股數}
    private Map<String, BigDecimal> prices; // 當日股價 {股票代號: 價格}
    private BigDecimal totalValue;          // 總價值
    private BigDecimal dailyReturn;         // 當日報酬率
}
