package com.stock_management.account_service.service;

import com.stock_management.account_service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    @Override
    public PieChartResponseDto getPortfolioPieChart() {
        try {
            PortfolioResponseDto portfolio = getPortfolio();
            
            // 按股票代碼分組並計算總現值
            Map<String, BigDecimal> stockTotals = portfolio.getPositions().stream()
                .collect(Collectors.groupingBy(
                    PositionDto::getCode,
                    Collectors.reducing(BigDecimal.ZERO, 
                        pos -> pos.getCurrentPrice().multiply(new BigDecimal(pos.getQuantity())), 
                        BigDecimal::add)
                ));

            // 計算總現值
            BigDecimal totalValue = stockTotals.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 轉換為圓餅圖資料
            List<ChartDataDto> chartData = stockTotals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10) // 只顯示前10大持股
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

            // 如果有更多持股，歸類為「其他」
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
            
        } catch (Exception e) {
            log.error("取得持股圓餅圖資料失敗", e);
            throw new RuntimeException("取得持股圓餅圖資料失敗", e);
        }
    }

    @Override
    public PortfolioResponseDto getPortfolio() {
        // 去除 dataProviderUrl 前後空白
        String baseUrl = dataProviderUrl.trim();
        // 確保不會有多餘的斜線或空白
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
        PortfolioResponseDto portfolio = getPortfolio();
        
        // 計算總成本
        BigDecimal totalCostValue = portfolio.getPositions().stream()
            .map(pos -> pos.getAvgPrice().multiply(new BigDecimal(pos.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 計算總現值
        BigDecimal totalMarketValue = portfolio.getPositions().stream()
            .map(pos -> pos.getCurrentPrice().multiply(new BigDecimal(pos.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 計算總未實現損益
        BigDecimal totalUnrealizedPnl = portfolio.getPositions().stream()
            .map(PositionDto::getUnrealizedPnl)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 計算總損益率
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
        // 簡單的顏色生成邏輯，可根據需求調整
        String[] colors = {
            "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF",
            "#FF9F40", "#FF6384", "#C9CBCF", "#4BC0C0", "#FF6384"
        };
        return colors[Math.abs(stockCode.hashCode()) % colors.length];
    }
}
