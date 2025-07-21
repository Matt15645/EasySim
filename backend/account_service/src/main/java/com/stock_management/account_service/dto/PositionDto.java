package com.stock_management.account_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class PositionDto {
    private String code;
    private Integer quantity;

    @JsonProperty("avg_price")
    private BigDecimal avgPrice;

    @JsonProperty("current_price")
    private BigDecimal currentPrice;

    @JsonProperty("unrealized_pnl")
    private BigDecimal unrealizedPnl;
}