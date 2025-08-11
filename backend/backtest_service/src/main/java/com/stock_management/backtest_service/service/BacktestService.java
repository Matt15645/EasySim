package com.stock_management.backtest_service.service;

import com.stock_management.backtest_service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BacktestService {

    private final DataProviderService dataProviderService;
    private final PortfolioCalculationService portfolioCalculationService;

    /**
     * 執行回測分析
     */
    public BacktestResponseDto performBacktest(BacktestRequestDto request) {
        try {
            log.info("開始執行回測，股票: {}, 期間: {} 到 {}, 起始資金: {}", 
                     request.getSymbols(), request.getStartDate(), request.getEndDate(), 
                     request.getInitialCapital());

            // 取得歷史股價資料
            Map<String, List<Map<String, Object>>> rawData = dataProviderService.getHistoricalData(
                request.getSymbols(), 
                request.getStartDate(), 
                request.getEndDate()
            );

            // 轉換股價資料格式
            Map<String, Map<String, BigDecimal>> priceData = convertPriceData(rawData);
            
            // 取得所有交易日期
            Set<String> allDates = priceData.values().stream()
                    .flatMap(stockPrices -> stockPrices.keySet().stream())
                    .collect(Collectors.toSet());
            List<String> sortedDates = allDates.stream().sorted().collect(Collectors.toList());

            // 執行回測模擬
            List<PortfolioSnapshot> portfolioHistory = simulateBacktest(
                    request, priceData, sortedDates);

            // 建立回應物件
            BacktestResponseDto response = new BacktestResponseDto();
            response.setPortfolioHistory(portfolioHistory);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("回測執行成功");

            // 計算回測結果並設置到回應物件中
            portfolioCalculationService.calculateBacktestResult(
                    response, portfolioHistory, request.getInitialCapital());

            log.info("回測執行完成，總報酬率: {}%", response.getReturnRate());
            return response;

        } catch (Exception e) {
            log.error("執行回測時發生錯誤: {}", e.getMessage(), e);
            
            BacktestResponseDto errorResponse = new BacktestResponseDto();
            errorResponse.setTimestamp(LocalDateTime.now());
            errorResponse.setMessage("回測執行失敗: " + e.getMessage());
            
            return errorResponse;
        }
    }

    /**
     * 轉換原始股價資料格式
     */
    private Map<String, Map<String, BigDecimal>> convertPriceData(
            Map<String, List<Map<String, Object>>> rawData) {
        
        Map<String, Map<String, BigDecimal>> priceData = new HashMap<>();
        
        for (Map.Entry<String, List<Map<String, Object>>> entry : rawData.entrySet()) {
            String symbol = entry.getKey();
            Map<String, BigDecimal> stockPrices = new HashMap<>();
            
            for (Map<String, Object> dataPoint : entry.getValue()) {
                String date = (String) dataPoint.get("date");
                Double closePrice = (Double) dataPoint.get("close");
                stockPrices.put(date, BigDecimal.valueOf(closePrice));
            }
            
            priceData.put(symbol, stockPrices);
        }
        
        return priceData;
    }

    /**
     * 執行回測模擬
     */
    private List<PortfolioSnapshot> simulateBacktest(
            BacktestRequestDto request,
            Map<String, Map<String, BigDecimal>> priceData,
            List<String> sortedDates) {

        List<PortfolioSnapshot> portfolioHistory = new ArrayList<>();
        
        // 初始化投資組合
        BigDecimal cash = request.getInitialCapital();
        Map<String, Integer> holdings = new HashMap<>();
        
        // 建立交易動作索引
        Map<String, List<TradeAction>> tradesByDate = request.getTradeActions().stream()
                .collect(Collectors.groupingBy(TradeAction::getDate));

        BigDecimal previousValue = request.getInitialCapital();

        for (String date : sortedDates) {
            // 取得當日股價
            Map<String, BigDecimal> dailyPrices = new HashMap<>();
            for (String symbol : request.getSymbols()) {
                if (priceData.containsKey(symbol) && priceData.get(symbol).containsKey(date)) {
                    dailyPrices.put(symbol, priceData.get(symbol).get(date));
                }
            }

            // 執行當日交易
            if (tradesByDate.containsKey(date)) {
                for (TradeAction trade : tradesByDate.get(date)) {
                    cash = executeTrade(trade, cash, holdings, dailyPrices);
                }
            }

            // 計算當日投資組合價值
            BigDecimal totalValue = calculatePortfolioValue(cash, holdings, dailyPrices);
            
            // 計算當日報酬率
            BigDecimal dailyReturn = BigDecimal.ZERO;
            if (previousValue.compareTo(BigDecimal.ZERO) > 0) {
                dailyReturn = totalValue.subtract(previousValue)
                                      .divide(previousValue, 6, RoundingMode.HALF_UP);
            }

            // 建立投資組合快照
            PortfolioSnapshot snapshot = new PortfolioSnapshot();
            snapshot.setDate(date);
            snapshot.setCash(cash);
            snapshot.setHoldings(new HashMap<>(holdings));
            snapshot.setPrices(new HashMap<>(dailyPrices));
            snapshot.setTotalValue(totalValue);
            snapshot.setDailyReturn(dailyReturn);

            portfolioHistory.add(snapshot);
            previousValue = totalValue;
        }

        return portfolioHistory;
    }

    /**
     * 執行交易
     */
    private BigDecimal executeTrade(TradeAction trade, BigDecimal cash, 
                                   Map<String, Integer> holdings, 
                                   Map<String, BigDecimal> dailyPrices) {
        
        if (!dailyPrices.containsKey(trade.getSymbol())) {
            log.warn("無法取得股票 {} 在 {} 的價格，交易取消", trade.getSymbol(), trade.getDate());
            return cash;
        }

        BigDecimal price = dailyPrices.get(trade.getSymbol());
        BigDecimal tradeValue = price.multiply(BigDecimal.valueOf(trade.getShares()));

        if (trade.getAction() == TradeAction.TradeType.BUY) {
            if (cash.compareTo(tradeValue) >= 0) {
                cash = cash.subtract(tradeValue);
                holdings.put(trade.getSymbol(), 
                           holdings.getOrDefault(trade.getSymbol(), 0) + trade.getShares());
                log.debug("買入 {} 股 {}，價格: {}，總金額: {}", 
                         trade.getShares(), trade.getSymbol(), price, tradeValue);
            } else {
                log.warn("資金不足，無法買入 {} 股 {}", trade.getShares(), trade.getSymbol());
            }
        } else if (trade.getAction() == TradeAction.TradeType.SELL) {
            int currentHolding = holdings.getOrDefault(trade.getSymbol(), 0);
            if (currentHolding >= trade.getShares()) {
                cash = cash.add(tradeValue);
                holdings.put(trade.getSymbol(), currentHolding - trade.getShares());
                log.debug("賣出 {} 股 {}，價格: {}，總金額: {}", 
                         trade.getShares(), trade.getSymbol(), price, tradeValue);
            } else {
                log.warn("持股不足，無法賣出 {} 股 {}", trade.getShares(), trade.getSymbol());
            }
        }

        return cash;
    }

    /**
     * 計算投資組合總價值
     */
    private BigDecimal calculatePortfolioValue(BigDecimal cash, 
                                              Map<String, Integer> holdings, 
                                              Map<String, BigDecimal> dailyPrices) {
        BigDecimal totalValue = cash;
        
        for (Map.Entry<String, Integer> holding : holdings.entrySet()) {
            String symbol = holding.getKey();
            Integer shares = holding.getValue();
            
            if (shares > 0 && dailyPrices.containsKey(symbol)) {
                BigDecimal stockValue = dailyPrices.get(symbol)
                                                  .multiply(BigDecimal.valueOf(shares));
                totalValue = totalValue.add(stockValue);
            }
        }
        
        return totalValue;
    }
}
