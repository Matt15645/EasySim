package com.stock_management.backtest_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BacktestRequestDto {
    private List<String> symbols;  // 股票代號列表，例如 ["2330", "2317"]
    private String startDate;      // 開始日期，格式: "2025-07-01"
    private String endDate;        // 結束日期，格式: "2025-07-22"
}
