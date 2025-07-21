package com.stock_management.account_service.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PortfolioResponseDto {
    private List<PositionDto> positions;
    private LocalDateTime timestamp;
}
