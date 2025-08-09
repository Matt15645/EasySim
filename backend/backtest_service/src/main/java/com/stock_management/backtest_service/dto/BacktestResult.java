package com.stock_management.backtest_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResult {
    private BigDecimal initialCapital;      // 起始資金
    private BigDecimal finalValue;          // 最終投資組合價值
    private BigDecimal totalReturn;         // 總報酬（金額）
    private BigDecimal returnRate;          // 報酬率（%）
    private BigDecimal annualizedSharpeRatio; // 年化 Sharpe Ratio
    private BigDecimal maxDrawdown;         // 最大回撤（%）
    private int tradingDays;                // 交易天數
}
