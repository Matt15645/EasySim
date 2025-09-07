package com.stock_management.subscribe_service.service;

import com.stock_management.subscribe_service.dto.ScannerRequest;
import com.stock_management.subscribe_service.dto.ScannerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Scanner Service Tests")
class ScannerServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ScannerService scannerService;

    private ScannerRequest validRequest;
    private Map<String, Object> mockResponseBody;

    @BeforeEach
    void setUp() {
        // 設定 data provider URL
        ReflectionTestUtils.setField(scannerService, "dataProviderUrl", "http://localhost:8000");
        ReflectionTestUtils.setField(scannerService, "restTemplate", restTemplate);

        // 創建有效的請求
        validRequest = ScannerRequest.builder()
                .scannerType("top_volume")
                .date("2025-07-01")
                .count(10)
                .ascending(false)
                .build();

        // 創建模擬回應資料
        List<Map<String, Object>> mockData = Arrays.asList(
                createStockData("AAPL", 150.0, 1000000),
                createStockData("GOOGL", 2520.0, 800000)
        );

        mockResponseBody = new HashMap<>();
        mockResponseBody.put("data", mockData);
    }

    private Map<String, Object> createStockData(String symbol, Double price, Integer volume) {
        Map<String, Object> stockData = new HashMap<>();
        stockData.put("symbol", symbol);
        stockData.put("price", price);
        stockData.put("volume", volume);
        return stockData;
    }

    @Test
    @DisplayName("取得掃描器資料成功 - 應該返回正確的掃描器結果")
    void shouldGetScannerDataSuccessfully() {
        // given
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        // when
        ScannerResponse result = scannerService.getScannerData(validRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getScannerType()).isEqualTo("top_volume");
        assertThat(result.getDate()).isEqualTo("2025-07-01");
        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getTimestamp()).isNotNull();
        
        // 驗證資料內容
        assertThat(result.getData().get(0).get("symbol")).isEqualTo("AAPL");
        assertThat(result.getData().get(1).get("symbol")).isEqualTo("GOOGL");
    }

    @Test
    @DisplayName("data-provider 回應空資料 - 應該返回空的掃描器結果")
    void shouldHandleEmptyDataResponse() {
        // given
        Map<String, Object> emptyResponseBody = new HashMap<>();
        emptyResponseBody.put("data", Arrays.asList());
        
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(emptyResponseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        // when
        ScannerResponse result = scannerService.getScannerData(validRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isEmpty();
        assertThat(result.getCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("data-provider 回應null資料 - 應該返回空的掃描器結果")
    void shouldHandleNullDataResponse() {
        // given
        Map<String, Object> nullResponseBody = new HashMap<>();
        nullResponseBody.put("data", null);
        
        ResponseEntity<Object> mockResponse = new ResponseEntity<>(nullResponseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        // when
        ScannerResponse result = scannerService.getScannerData(validRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getData()).isNull();
        assertThat(result.getCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("data-provider 連接失敗 - 應該拋出RuntimeException")
    void shouldThrowExceptionWhenDataProviderConnectionFails() {
        // given
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new RestClientException("Connection failed"));

        // when & then
        assertThatThrownBy(() -> scannerService.getScannerData(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("請求掃描器資料失敗");
    }

    @Test
    @DisplayName("data-provider 回應非2xx狀態碼 - 應該拋出RuntimeException")
    void shouldThrowExceptionWhenDataProviderResponseNon2xx() {
        // given
        ResponseEntity<Object> mockResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        // when & then
        assertThatThrownBy(() -> scannerService.getScannerData(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("data-provider 回應異常");
    }

    @Test
    @DisplayName("data-provider 回應null body - 應該拋出RuntimeException")
    void shouldThrowExceptionWhenDataProviderResponseNullBody() {
        // given
        ResponseEntity<Object> mockResponse = ResponseEntity.ok().build();
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        // when & then
        assertThatThrownBy(() -> scannerService.getScannerData(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("data-provider 回應異常");
    }

    @Test
    @DisplayName("請求不同掃描器類型 - 應該正確處理")
    void shouldHandleDifferentScannerTypes() {
        // given
        ScannerRequest topPriceRequest = ScannerRequest.builder()
                .scannerType("top_price")
                .date("2025-07-01")
                .count(5)
                .ascending(true)
                .build();

        ResponseEntity<Object> mockResponse = new ResponseEntity<>(mockResponseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        // when
        ScannerResponse result = scannerService.getScannerData(topPriceRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getScannerType()).isEqualTo("top_price");
        assertThat(result.getData()).hasSize(2);
    }

    @Test
    @DisplayName("大量資料請求 - 應該正確處理")
    void shouldHandleLargeDataRequest() {
        // given
        ScannerRequest largeRequest = ScannerRequest.builder()
                .scannerType("top_volume")
                .date("2025-07-01")
                .count(200)
                .ascending(false)
                .build();

        // 創建大量資料
        List<Map<String, Object>> largeData = new java.util.ArrayList<>();
        for (int i = 0; i < 200; i++) {
            largeData.add(createStockData("STOCK" + i, 100.0 + i, 1000000 - i * 1000));
        }

        Map<String, Object> largeResponseBody = new HashMap<>();
        largeResponseBody.put("data", largeData);

        ResponseEntity<Object> mockResponse = new ResponseEntity<>(largeResponseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        // when
        ScannerResponse result = scannerService.getScannerData(largeRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCount()).isEqualTo(200);
        assertThat(result.getData()).hasSize(200);
    }
}
