package com.stock_management.account_service.service;

import com.stock_management.account_service.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final WebClient webClient;

    @Value("${data-provider.url}")
    private String dataProviderUrl;

    private Long getCurrentUserId() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            String userId = request.getHeader("X-User-Id");
            if (userId != null && !userId.isEmpty()) {
                return Long.valueOf(userId);
            }
        }
        log.warn("X-User-Id header not found. This should not happen in production.");
        throw new RuntimeException("User ID not found in request header.");
    }

    @Override
    public PieChartResponseDto getPortfolioPieChart() {
        Long userId = getCurrentUserId();
        log.info("Fetching pie chart for user ID: {}", userId);

        PortfolioResponseDto portfolio = getPortfolio();
        
        Map<String, BigDecimal> stockTotals = portfolio.getPositions().stream()
            .collect(Collectors.groupingBy(
                PositionDto::getCode,
                Collectors.reducing(BigDecimal.ZERO, 
                    pos -> pos.getCurrentPrice().multiply(new BigDecimal(pos.getQuantity())), 
                    BigDecimal::add)
            ));

        BigDecimal totalValue = stockTotals.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ChartDataDto> chartData = stockTotals.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
            .limit(10)
            .map(entry -> {
                ChartDataDto dto = new ChartDataDto();
                dto.setLabel(entry.getKey());
                dto.setValue(entry.getValue());
                dto.setPercentage(entry.getValue()
                    .divide(totalValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")));
                dto.setColor(generateColor(entry.getKey()));
                return dto;
            })
            .collect(Collectors.toList());

        if (stockTotals.size() > 10) {
            BigDecimal otherValue = stockTotals.entrySet().stream()
                .skip(10)
                .map(Map.Entry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            ChartDataDto otherDto = new ChartDataDto();
            otherDto.setLabel("其他");
            otherDto.setValue(otherValue);
            otherDto.setPercentage(otherValue
                .divide(totalValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")));
            otherDto.setColor("#CCCCCC");
            chartData.add(otherDto);
        }

        PieChartResponseDto response = new PieChartResponseDto();
        response.setTotalValue(totalValue);
        response.setPositions(chartData);
        
        return response;
        
    }

    @Override
    public PortfolioResponseDto getPortfolio() {
        String baseUrl = dataProviderUrl.trim();
        String url = baseUrl.endsWith("/") ? baseUrl + "api/positions" : baseUrl + "/api/positions";
        log.info("Requesting: {}", url);
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(PortfolioResponseDto.class)
            .block();
    }

    @Override
    public PortfolioSummaryDto getPortfolioSummary() {
        Long userId = getCurrentUserId();
        log.info("Fetching summary for user ID: {}", userId);

        PortfolioResponseDto portfolio = getPortfolio();
        
        BigDecimal totalCostValue = portfolio.getPositions().stream()
            .map(pos -> pos.getAvgPrice().multiply(new BigDecimal(pos.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalMarketValue = portfolio.getPositions().stream()
            .map(pos -> pos.getCurrentPrice().multiply(new BigDecimal(pos.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalUnrealizedPnl = portfolio.getPositions().stream()
            .map(PositionDto::getUnrealizedPnl)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalProfitLossRatio = BigDecimal.ZERO;
        if (totalCostValue.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossRatio = totalUnrealizedPnl
                .divide(totalCostValue, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }
        
        PortfolioSummaryDto summary = new PortfolioSummaryDto();
        summary.setPositions(portfolio.getPositions());
        summary.setTotalCostValue(totalCostValue);
        summary.setTotalMarketValue(totalMarketValue);
        summary.setTotalUnrealizedPnl(totalUnrealizedPnl);
        summary.setTotalProfitLossRatio(totalProfitLossRatio);
        
        return summary;
    }

    private String generateColor(String stockCode) {
        String[] colors = {
            "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF",
            "#FF9F40", "#FF6384", "#C9CBCF", "#4BC0C0", "#FF6384"
        };
        return colors[Math.abs(stockCode.hashCode()) % colors.length];
    }
}
