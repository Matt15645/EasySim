package com.stock_management.backtest_service.service;

import com.stock_management.backtest_service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BacktestService {

    private final DataProviderService dataProviderService;

    /**
     * 執行回測分析
     */
    public BacktestResponseDto performBacktest(BacktestRequestDto request) {
        try {
            log.info("開始執行回測，股票: {}, 期間: {} 到 {}", 
                     request.getSymbols(), request.getStartDate(), request.getEndDate());

            // 使用批次歷史資料端點
            Map<String, List<Map<String, Object>>> rawData = dataProviderService.getHistoricalData(
                request.getSymbols(), 
                request.getStartDate(), 
                request.getEndDate()
            );

            // 處理並轉換資料格式
            List<StockSeriesData> seriesDataList = processRawData(rawData);

            BacktestResponseDto response = new BacktestResponseDto();
            response.setSeries(seriesDataList);
            response.setTimestamp(LocalDateTime.now());
            response.setMessage("回測資料準備完成");

            log.info("回測執行完成，共處理 {} 支股票的資料", seriesDataList.size());
            return response;

        } catch (Exception e) {
            log.error("執行回測時發生錯誤: {}", e.getMessage(), e);
            
            BacktestResponseDto errorResponse = new BacktestResponseDto();
            errorResponse.setSeries(new ArrayList<>());
            errorResponse.setTimestamp(LocalDateTime.now());
            errorResponse.setMessage("回測執行失敗: " + e.getMessage());
            
            return errorResponse;
        }
    }

    /**
     * 處理原始資料並轉換為前端需要的格式
     */
    private List<StockSeriesData> processRawData(Map<String, List<Map<String, Object>>> rawData) {
        List<StockSeriesData> seriesDataList = new ArrayList<>();

        for (Map.Entry<String, List<Map<String, Object>>> entry : rawData.entrySet()) {
            String symbol = entry.getKey();
            List<Map<String, Object>> stockDataPoints = entry.getValue();

            List<StockDataPoint> dataPoints = new ArrayList<>();
            
            for (Map<String, Object> point : stockDataPoints) {
                StockDataPoint dataPoint = new StockDataPoint();
                dataPoint.setDate((String) point.get("date"));
                dataPoint.setClosePrice(BigDecimal.valueOf((Double) point.get("close")));
                dataPoint.setTimestamp((Long) point.get("ts"));
                
                dataPoints.add(dataPoint);
            }

            // 按日期排序
            dataPoints.sort((a, b) -> a.getDate().compareTo(b.getDate()));

            StockSeriesData seriesData = new StockSeriesData();
            seriesData.setSymbol(symbol);
            seriesData.setDataPoints(dataPoints);

            seriesDataList.add(seriesData);
            
            log.debug("處理股票 {} 的資料，共 {} 個資料點", symbol, dataPoints.size());
        }

        return seriesDataList;
    }
}
