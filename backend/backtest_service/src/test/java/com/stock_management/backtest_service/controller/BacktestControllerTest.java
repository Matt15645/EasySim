package com.stock_management.backtest_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock_management.backtest_service.dto.BacktestRequestDto;
import com.stock_management.backtest_service.dto.BacktestResponseDto;
import com.stock_management.backtest_service.dto.TradeAction;
import com.stock_management.backtest_service.service.BacktestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Backtest Controller Tests")
class BacktestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BacktestService backtestService;

    @InjectMocks
    private BacktestController backtestController;

    private ObjectMapper objectMapper;
    private BacktestRequestDto validRequest;
    private BacktestResponseDto mockResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(backtestController).build();
        
        // 創建有效的請求
        TradeAction buyAction = new TradeAction();
        buyAction.setDate("2025-07-01");
        buyAction.setSymbol("AAPL");
        buyAction.setAction(TradeAction.TradeType.BUY);
        buyAction.setShares(100);

        validRequest = new BacktestRequestDto();
        validRequest.setSymbols(Arrays.asList("AAPL", "GOOGL"));
        validRequest.setStartDate("2025-07-01");
        validRequest.setEndDate("2025-07-31");
        validRequest.setInitialCapital(BigDecimal.valueOf(100000.00));
        validRequest.setTradeActions(Arrays.asList(buyAction));

        // 創建模擬回應
        mockResponse = new BacktestResponseDto();
        mockResponse.setInitialCapital(BigDecimal.valueOf(100000.00));
        mockResponse.setFinalValue(BigDecimal.valueOf(110000.00));
        mockResponse.setTotalReturn(BigDecimal.valueOf(10000.00));
        mockResponse.setReturnRate(BigDecimal.valueOf(10.00));
        mockResponse.setTradingDays(31);
        mockResponse.setTimestamp(LocalDateTime.now());
        mockResponse.setMessage("回測執行成功");
    }

    @Test
    @DisplayName("執行回測成功 - 應該返回200狀態碼和結果")
    void shouldPerformBacktestSuccessfully() throws Exception {
        // given
        when(backtestService.performBacktest(any(BacktestRequestDto.class)))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/backtest/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.initialCapital").value(100000.00))
                .andExpect(jsonPath("$.finalValue").value(110000.00))
                .andExpect(jsonPath("$.returnRate").value(10.00))
                .andExpect(jsonPath("$.message").value("回測執行成功"));
    }

    @Test
    @DisplayName("健康檢查 - 應該返回200狀態碼")
    void shouldReturnHealthCheckSuccessfully() throws Exception {
        mockMvc.perform(get("/api/backtest/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Backtest Service is running"));
    }

    @Test
    @DisplayName("無效請求 - 缺少股票代號")
    void shouldFailWhenSymbolsAreNull() throws Exception {
        // given
        validRequest.setSymbols(null);

        // when & then
        mockMvc.perform(post("/api/backtest/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("無效請求 - 空的股票代號列表")
    void shouldFailWhenSymbolsAreEmpty() throws Exception {
        // given
        validRequest.setSymbols(Collections.emptyList());

        // when & then
        mockMvc.perform(post("/api/backtest/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("無效請求 - 缺少開始日期")
    void shouldFailWhenStartDateIsNull() throws Exception {
        // given
        validRequest.setStartDate(null);

        // when & then
        mockMvc.perform(post("/api/backtest/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("無效請求 - 缺少結束日期")
    void shouldFailWhenEndDateIsNull() throws Exception {
        // given
        validRequest.setEndDate(null);

        // when & then
        mockMvc.perform(post("/api/backtest/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("服務異常 - 應該返回500狀態碼")
    void shouldReturnInternalServerErrorWhenServiceThrowsException() throws Exception {
        // given
        when(backtestService.performBacktest(any(BacktestRequestDto.class)))
                .thenThrow(new RuntimeException("Internal server error"));

        // when & then
        mockMvc.perform(post("/api/backtest/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("無效JSON - 應該返回400狀態碼")
    void shouldFailWithInvalidJson() throws Exception {
        mockMvc.perform(post("/api/backtest/analyze")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("缺少Content-Type - 應該返回415狀態碼")
    void shouldFailWithoutContentType() throws Exception {
        mockMvc.perform(post("/api/backtest/analyze")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }
}
