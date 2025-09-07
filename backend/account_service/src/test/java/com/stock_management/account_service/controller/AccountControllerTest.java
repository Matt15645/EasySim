package com.stock_management.account_service.controller;

import com.stock_management.account_service.dto.*;
import com.stock_management.account_service.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(controllers = AccountController.class, 
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
    })
@DisplayName("Account Controller Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    private PortfolioResponseDto mockPortfolio;
    private PieChartResponseDto mockPieChart;
    private PortfolioSummaryDto mockSummary;

    @BeforeEach
    void setUp() {
        // 設定測試數據
        setupMockPortfolio();
        setupMockPieChart();
        setupMockSummary();
    }

    private void setupMockPortfolio() {
        List<PositionDto> positions = Arrays.asList(
            createPosition("2330", 100, new BigDecimal("550.00"), new BigDecimal("580.00")),
            createPosition("006208", 200, new BigDecimal("105.50"), new BigDecimal("123.35"))
        );
        mockPortfolio = new PortfolioResponseDto();
        mockPortfolio.setPositions(positions);
    }

    private void setupMockPieChart() {
        List<ChartDataDto> chartData = Arrays.asList(
            createChartData("2330", new BigDecimal("58000"), new BigDecimal("67.44"), "#FF6384"),
            createChartData("006208", new BigDecimal("24670"), new BigDecimal("32.56"), "#9966FF")
        );
        mockPieChart = new PieChartResponseDto();
        mockPieChart.setTotalValue(new BigDecimal("82670"));
        mockPieChart.setPositions(chartData);
    }

    private void setupMockSummary() {
        mockSummary = new PortfolioSummaryDto();
        mockSummary.setTotalMarketValue(new BigDecimal("82670"));
        mockSummary.setTotalCostValue(new BigDecimal("76100"));
        mockSummary.setTotalUnrealizedPnl(new BigDecimal("6570"));
        mockSummary.setTotalProfitLossRatio(new BigDecimal("8.63"));
    }

    private PositionDto createPosition(String code, Integer quantity, BigDecimal avgPrice, BigDecimal currentPrice) {
        PositionDto position = new PositionDto();
        position.setCode(code);
        position.setQuantity(quantity);
        position.setAvgPrice(avgPrice);
        position.setCurrentPrice(currentPrice);
        position.setUnrealizedPnl(currentPrice.subtract(avgPrice).multiply(new BigDecimal(quantity)));
        return position;
    }

    private ChartDataDto createChartData(String label, BigDecimal value, BigDecimal percentage, String color) {
        ChartDataDto data = new ChartDataDto();
        data.setLabel(label);
        data.setValue(value);
        data.setPercentage(percentage);
        data.setColor(color);
        return data;
    }

    @Test
    @DisplayName("GET /api/account/portfolio - 應該成功返回投資組合")
    void shouldReturnPortfolioSuccessfully() throws Exception {
        // Arrange
        when(accountService.getPortfolio()).thenReturn(mockPortfolio);

        // Act & Assert
        mockMvc.perform(get("/api/account/portfolio")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.positions", hasSize(2)))
                .andExpect(jsonPath("$.positions[0].code", is("2330")))
                .andExpect(jsonPath("$.positions[0].quantity", is(100)))
                .andExpect(jsonPath("$.positions[1].code", is("006208")))
                .andExpect(jsonPath("$.positions[1].quantity", is(200)));
    }

    @Test
    @DisplayName("GET /api/account/portfolio/pie-chart - 應該成功返回圓餅圖數據")
    void shouldReturnPieChartDataSuccessfully() throws Exception {
        // Arrange
        when(accountService.getPortfolioPieChart()).thenReturn(mockPieChart);

        // Act & Assert
        mockMvc.perform(get("/api/account/portfolio/pie-chart")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalValue", is(82670)))
                .andExpect(jsonPath("$.positions", hasSize(2)))
                .andExpect(jsonPath("$.positions[0].label", is("2330")))
                .andExpect(jsonPath("$.positions[0].value", is(58000)))
                .andExpect(jsonPath("$.positions[0].percentage", is(67.44)))
                .andExpect(jsonPath("$.positions[0].color", is("#FF6384")));
    }

    @Test
    @DisplayName("GET /api/account/portfolio/summary - 應該成功返回投資組合摘要")
    void shouldReturnPortfolioSummarySuccessfully() throws Exception {
        // Arrange
        when(accountService.getPortfolioSummary()).thenReturn(mockSummary);

        // Act & Assert
        mockMvc.perform(get("/api/account/portfolio/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalMarketValue", is(82670)))
                .andExpect(jsonPath("$.totalCostValue", is(76100)))
                .andExpect(jsonPath("$.totalUnrealizedPnl", is(6570)))
                .andExpect(jsonPath("$.totalProfitLossRatio", is(8.63)));
    }

    @Test
    @DisplayName("服務拋出異常時應該返回500錯誤")
    void shouldReturn500WhenServiceThrowsException() throws Exception {
        // Arrange
        when(accountService.getPortfolio()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/account/portfolio")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
