package com.stock_management.backtest_service.service;

import com.stock_management.backtest_service.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PortfolioCalculationService {

    /**
     * 計算投資組合回測結果
     */
    public void calculateBacktestResult(
            BacktestResponseDto response,
            List<PortfolioSnapshot> portfolioHistory,
            BigDecimal initialCapital) {

        if (portfolioHistory.isEmpty()) {
            throw new IllegalArgumentException("投資組合歷史資料為空");
        }

        response.setInitialCapital(initialCapital);

        // 最終價值
        PortfolioSnapshot finalSnapshot = portfolioHistory.get(portfolioHistory.size() - 1);
        response.setFinalValue(finalSnapshot.getTotalValue());

        // 總報酬和報酬率
        BigDecimal totalReturn = finalSnapshot.getTotalValue().subtract(initialCapital);
        response.setTotalReturn(totalReturn);
        response.setReturnRate(totalReturn.divide(initialCapital, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)));

        // 交易天數
        response.setTradingDays(portfolioHistory.size());

        // 年化 Sharpe Ratio
        response.setAnnualizedSharpeRatio(calculateAnnualizedSharpeRatio(portfolioHistory));

        // 最大回撤
        response.setMaxDrawdown(calculateMaxDrawdown(portfolioHistory));
    }

    /**
     * 計算年化 Sharpe Ratio
     */
    private BigDecimal calculateAnnualizedSharpeRatio(List<PortfolioSnapshot> portfolioHistory) {
        if (portfolioHistory.size() < 2) {
            return BigDecimal.ZERO;
        }

        // 計算每日報酬率
        List<BigDecimal> dailyReturns = portfolioHistory.stream()
                .map(PortfolioSnapshot::getDailyReturn)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (dailyReturns.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 平均日報酬率
        BigDecimal meanReturn = dailyReturns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyReturns.size()), 6, RoundingMode.HALF_UP);

        // 標準差
        BigDecimal variance = dailyReturns.stream()
                .map(ret -> ret.subtract(meanReturn).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyReturns.size()), 6, RoundingMode.HALF_UP);

        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));

        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 年化 Sharpe Ratio (假設無風險利率為0，252個交易日)
        BigDecimal annualizedReturn = meanReturn.multiply(BigDecimal.valueOf(252));
        BigDecimal annualizedStdDev = stdDev.multiply(BigDecimal.valueOf(Math.sqrt(252)));

        return annualizedReturn.divide(annualizedStdDev, 4, RoundingMode.HALF_UP);
    }

    /**
     * 計算最大回撤
     */
    private BigDecimal calculateMaxDrawdown(List<PortfolioSnapshot> portfolioHistory) {
        BigDecimal maxValue = BigDecimal.ZERO;
        BigDecimal maxDrawdown = BigDecimal.ZERO;

        for (PortfolioSnapshot snapshot : portfolioHistory) {
            BigDecimal currentValue = snapshot.getTotalValue();

            if (currentValue.compareTo(maxValue) > 0) {
                maxValue = currentValue;
            }

            BigDecimal drawdown = maxValue.subtract(currentValue)
                    .divide(maxValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }

        return maxDrawdown;
    }
}
