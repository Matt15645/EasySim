package com.stock_management.account_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PieChartResponseDto {
    private BigDecimal totalValue;
    private List<ChartDataDto> positions;
}
