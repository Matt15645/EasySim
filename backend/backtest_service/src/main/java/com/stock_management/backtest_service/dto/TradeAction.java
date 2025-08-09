package com.stock_management.backtest_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeAction {
    private String date;           // 交易日期，格式: "2025-07-15"
    private String symbol;         // 股票代號
    private TradeType action;      // 交易類型：BUY, SELL
    private Integer shares;        // 股數
    
    public enum TradeType {
        BUY, SELL
    }
}
