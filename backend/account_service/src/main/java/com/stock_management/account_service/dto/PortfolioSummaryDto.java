package com.stock_management.account_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioSummaryDto {
    private List<PositionDto> positions;
    private BigDecimal totalMarketValue;    // 總現值
    private BigDecimal totalCostValue;      // 總成本
    private BigDecimal totalUnrealizedPnl;  // 總未實現損益
    private BigDecimal totalProfitLossRatio; // 總損益率
}
