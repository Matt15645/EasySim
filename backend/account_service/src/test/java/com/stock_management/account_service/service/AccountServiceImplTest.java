package com.stock_management.account_service.service;

import com.stock_management.account_service.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Account Service Tests")
class AccountServiceImplTest {

    @Mock
    private WebClient webClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private AccountServiceImpl accountService;

    private PortfolioResponseDto mockPortfolioResponse;

    @BeforeEach
    void setUp() {
        // 設定測試數據
        ReflectionTestUtils.setField(accountService, "dataProviderUrl", "http://test-data-provider:8000");
        
        // 建立模擬投資組合數據
        List<PositionDto> positions = Arrays.asList(
            createPosition("2330", 100, new BigDecimal("550.00"), new BigDecimal("580.00")),
            createPosition("006208", 200, new BigDecimal("105.50"), new BigDecimal("123.35")),
            createPosition("00757", 150, new BigDecimal("94.14"), new BigDecimal("114.25"))
        );
        
        mockPortfolioResponse = new PortfolioResponseDto();
        mockPortfolioResponse.setPositions(positions);
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

    @Test
    @DisplayName("應該成功獲取投資組合數據")
    void shouldGetPortfolioSuccessfully() {
        // Arrange
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PortfolioResponseDto.class)).thenReturn(Mono.just(mockPortfolioResponse));

        // Act
        PortfolioResponseDto result = accountService.getPortfolio();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPositions()).hasSize(3);
        assertThat(result.getPositions().get(0).getCode()).isEqualTo("2330");
        
        verify(webClient).get();
    }

    @Test
    @DisplayName("應該成功計算投資組合圓餅圖數據")
    void shouldCalculatePieChartDataSuccessfully() {
        // Arrange
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PortfolioResponseDto.class)).thenReturn(Mono.just(mockPortfolioResponse));

        // Act
        PieChartResponseDto result = accountService.getPortfolioPieChart();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalValue()).isPositive();
        assertThat(result.getPositions()).isNotEmpty();
        
        // 驗證圓餅圖數據按價值排序
        List<ChartDataDto> chartData = result.getPositions();
        for (int i = 0; i < chartData.size() - 1; i++) {
            assertThat(chartData.get(i).getValue())
                .isGreaterThanOrEqualTo(chartData.get(i + 1).getValue());
        }
        
        // 驗證百分比計算
        BigDecimal totalPercentage = chartData.stream()
            .map(ChartDataDto::getPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalPercentage).isLessThanOrEqualTo(new BigDecimal("100.01")); // 允許精度誤差
    }

    @Test
    @DisplayName("應該成功計算投資組合摘要")
    void shouldCalculatePortfolioSummarySuccessfully() {
        // Arrange
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PortfolioResponseDto.class)).thenReturn(Mono.just(mockPortfolioResponse));

        // Act
        PortfolioSummaryDto result = accountService.getPortfolioSummary();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalMarketValue()).isPositive();
        assertThat(result.getTotalCostValue()).isPositive();
        assertThat(result.getTotalUnrealizedPnl()).isNotNull();
        assertThat(result.getTotalProfitLossRatio()).isNotNull();
        
        // 驗證計算邏輯
        assertThat(result.getTotalMarketValue().subtract(result.getTotalCostValue()))
            .isEqualByComparingTo(result.getTotalUnrealizedPnl());
    }

    @Test
    @DisplayName("應該正確處理空的投資組合")
    void shouldHandleEmptyPortfolio() {
        // Arrange
        PortfolioResponseDto emptyPortfolio = new PortfolioResponseDto();
        emptyPortfolio.setPositions(Arrays.asList());
        
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PortfolioResponseDto.class)).thenReturn(Mono.just(emptyPortfolio));

        // Act
        PieChartResponseDto pieChart = accountService.getPortfolioPieChart();
        PortfolioSummaryDto summary = accountService.getPortfolioSummary();

        // Assert
        assertThat(pieChart.getTotalValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(pieChart.getPositions()).isEmpty();
        
        assertThat(summary.getTotalCostValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalMarketValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
