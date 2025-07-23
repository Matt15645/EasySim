package com.stock_management.backtest_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSeriesData {
    private String symbol;                    // 股票代號
    private List<StockDataPoint> dataPoints;  // 時間序列資料點
}
