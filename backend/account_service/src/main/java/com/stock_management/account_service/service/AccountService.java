package com.stock_management.account_service.service;

import com.stock_management.account_service.dto.PieChartResponseDto;
import com.stock_management.account_service.dto.PortfolioResponseDto;
import com.stock_management.account_service.dto.PortfolioSummaryDto;

public interface AccountService {
    PieChartResponseDto getPortfolioPieChart();
    PortfolioResponseDto getPortfolio();
    PortfolioSummaryDto getPortfolioSummary();
}
