package com.stock_management.account_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ChartDataDto {
    private String label;
    private BigDecimal value;
    private BigDecimal percentage;
    private String color;
}
