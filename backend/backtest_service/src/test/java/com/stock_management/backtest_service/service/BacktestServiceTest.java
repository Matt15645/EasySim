package com.stock_management.backtest_service.service;

import com.stock_management.backtest_service.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Backtest Service Tests")
class BacktestServiceTest {

    @Mock
    private DataProviderService dataProviderService;

    @Mock
    private PortfolioCalculationService portfolioCalculationService;

    @InjectMocks
    private BacktestService backtestService;

    private BacktestRequestDto backtestRequest;
    private Map<String, List<Map<String, Object>>> mockHistoricalData;

    @BeforeEach
    void setUp() {
        // 創建交易動作
        TradeAction buyAction = new TradeAction();
        buyAction.setDate("2025-07-01");
        buyAction.setSymbol("AAPL");
        buyAction.setAction(TradeAction.TradeType.BUY);
        buyAction.setShares(100);

        // 創建回測請求
        backtestRequest = new BacktestRequestDto();
        backtestRequest.setSymbols(Arrays.asList("AAPL", "GOOGL"));
        backtestRequest.setStartDate("2025-07-01");
        backtestRequest.setEndDate("2025-07-31");
        backtestRequest.setInitialCapital(BigDecimal.valueOf(100000.00));
        backtestRequest.setTradeActions(Arrays.asList(buyAction));

        // 創建模擬歷史數據
        setupMockHistoricalData();
    }

    private void setupMockHistoricalData() {
        mockHistoricalData = new HashMap<>();
        
        // AAPL歷史數據
        List<Map<String, Object>> aaplData = Arrays.asList(
            createDataPoint("2025-07-01", "150.00"),
            createDataPoint("2025-07-02", "152.00"),
            createDataPoint("2025-07-03", "155.00")
        );
        
        // GOOGL歷史數據
        List<Map<String, Object>> googlData = Arrays.asList(
            createDataPoint("2025-07-01", "2500.00"),
            createDataPoint("2025-07-02", "2520.00"),
            createDataPoint("2025-07-03", "2550.00")
        );
        
        mockHistoricalData.put("AAPL", aaplData);
        mockHistoricalData.put("GOOGL", googlData);
    }

    private Map<String, Object> createDataPoint(String date, String price) {
        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("date", date);
        dataPoint.put("close", Double.valueOf(price));  // 使用Double而不是String
        return dataPoint;
    }

    @Test
    @DisplayName("執行回測成功 - 應該返回正確的回測結果")
    void shouldPerformBacktestSuccessfully() {
        // given
        when(dataProviderService.getHistoricalData(anyList(), anyString(), anyString()))
                .thenReturn(mockHistoricalData);

        // when
        BacktestResponseDto result = backtestService.performBacktest(backtestRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.getMessage()).isEqualTo("回測執行成功");
        // 由於portfolioCalculationService會設置這些值，我們測試它們不是null
        assertThat(result.getPortfolioHistory()).isNotNull();
    }

    @Test
    @DisplayName("執行回測 - 空的股票代號列表")
    void shouldHandleEmptySymbolsList() {
        // given
        backtestRequest.setSymbols(Collections.emptyList());
        when(dataProviderService.getHistoricalData(anyList(), anyString(), anyString()))
                .thenReturn(Collections.emptyMap());

        // when
        BacktestResponseDto result = backtestService.performBacktest(backtestRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("回測執行成功");
    }

    @Test
    @DisplayName("執行回測 - null的股票代號列表")
    void shouldHandleNullSymbolsList() {
        // given
        backtestRequest.setSymbols(null);

        // when
        BacktestResponseDto result = backtestService.performBacktest(backtestRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("回測執行成功");
        // null symbols的情況下，服務仍能處理並返回成功
    }

    @Test
    @DisplayName("執行回測 - 資料提供者服務異常")
    void shouldHandleDataProviderServiceException() {
        // given
        when(dataProviderService.getHistoricalData(anyList(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Data provider connection failed"));

        // when
        BacktestResponseDto result = backtestService.performBacktest(backtestRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).contains("回測執行失敗");
        assertThat(result.getMessage()).contains("Data provider connection failed");
    }

    @Test
    @DisplayName("執行回測 - 無效的日期格式")
    void shouldHandleInvalidDateFormat() {
        // given
        backtestRequest.setStartDate("invalid-date");
        backtestRequest.setEndDate("2025-07-31");

        when(dataProviderService.getHistoricalData(anyList(), anyString(), anyString()))
                .thenReturn(mockHistoricalData);

        // when
        BacktestResponseDto result = backtestService.performBacktest(backtestRequest);

        // then
        assertThat(result).isNotNull();
        // 服務應該優雅地處理日期錯誤或返回適當的錯誤信息
    }

    @Test
    @DisplayName("執行回測 - 起始資金為負數")
    void shouldHandleNegativeInitialCapital() {
        // given
        backtestRequest.setInitialCapital(BigDecimal.valueOf(-1000.00));

        when(dataProviderService.getHistoricalData(anyList(), anyString(), anyString()))
                .thenReturn(mockHistoricalData);

        // when
        BacktestResponseDto result = backtestService.performBacktest(backtestRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("回測執行成功");
        assertThat(result.getPortfolioHistory()).isNotNull();
    }

    @Test
    @DisplayName("執行回測 - 無交易動作")
    void shouldHandleNoTradeActions() {
        // given
        backtestRequest.setTradeActions(Collections.emptyList());

        when(dataProviderService.getHistoricalData(anyList(), anyString(), anyString()))
                .thenReturn(mockHistoricalData);

        // when
        BacktestResponseDto result = backtestService.performBacktest(backtestRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("回測執行成功");
        assertThat(result.getPortfolioHistory()).isNotNull();
    }

    @Test
    @DisplayName("執行回測 - 複雜交易場景")
    void shouldHandleComplexTradingScenario() {
        // given
        TradeAction buyAction1 = new TradeAction("2025-07-01", "AAPL", TradeAction.TradeType.BUY, 100);
        TradeAction buyAction2 = new TradeAction("2025-07-02", "GOOGL", TradeAction.TradeType.BUY, 10);
        TradeAction sellAction = new TradeAction("2025-07-03", "AAPL", TradeAction.TradeType.SELL, 50);

        backtestRequest.setTradeActions(Arrays.asList(buyAction1, buyAction2, sellAction));

        when(dataProviderService.getHistoricalData(anyList(), anyString(), anyString()))
                .thenReturn(mockHistoricalData);

        // when
        BacktestResponseDto result = backtestService.performBacktest(backtestRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMessage()).isEqualTo("回測執行成功");
        assertThat(result.getPortfolioHistory()).isNotNull();
        assertThat(result.getPortfolioHistory()).hasSizeGreaterThan(0);
    }
}
