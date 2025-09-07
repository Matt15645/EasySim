package com.stock_management.subscribe_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock_management.subscribe_service.dto.ScannerRequest;
import com.stock_management.subscribe_service.dto.ScannerResponse;
import com.stock_management.subscribe_service.service.ScannerService;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Scanner Controller Tests")
class ScannerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ScannerService scannerService;

    @InjectMocks
    private ScannerController scannerController;

    private ObjectMapper objectMapper;
    private ScannerRequest validRequest;
    private ScannerResponse mockResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(scannerController).build();

        // 創建有效的請求
        validRequest = ScannerRequest.builder()
                .scannerType("top_volume")
                .date("2025-07-01")
                .count(10)
                .ascending(false)
                .build();

        // 創建模擬回應
        List<Map<String, Object>> mockData = Arrays.asList(
                createStockData("AAPL", 150.0, 1000000),
                createStockData("GOOGL", 2520.0, 800000)
        );

        mockResponse = ScannerResponse.builder()
                .data(mockData)
                .timestamp("2025-07-01T10:00:00")
                .scannerType("top_volume")
                .date("2025-07-01")
                .count(2)
                .build();
    }

    private Map<String, Object> createStockData(String symbol, Double price, Integer volume) {
        Map<String, Object> stockData = new HashMap<>();
        stockData.put("symbol", symbol);
        stockData.put("price", price);
        stockData.put("volume", volume);
        return stockData;
    }

    @Test
    @DisplayName("取得掃描器資料成功 - 應該返回200狀態碼和結果")
    void shouldGetScannerDataSuccessfully() throws Exception {
        // given
        when(scannerService.getScannerData(any(ScannerRequest.class)))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scannerType").value("top_volume"))
                .andExpect(jsonPath("$.date").value("2025-07-01"))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$.data[1].symbol").value("GOOGL"));
    }

    @Test
    @DisplayName("無效請求 - 缺少掃描器類型")
    void shouldReturnBadRequestWhenScannerTypeIsNull() throws Exception {
        // given
        ScannerRequest invalidRequest = ScannerRequest.builder()
                .scannerType(null)
                .date("2025-07-01")
                .count(10)
                .build();

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("無效請求 - 空的掃描器類型")
    void shouldReturnBadRequestWhenScannerTypeIsEmpty() throws Exception {
        // given
        ScannerRequest invalidRequest = ScannerRequest.builder()
                .scannerType("")
                .date("2025-07-01")
                .count(10)
                .build();

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("無效請求 - 缺少日期")
    void shouldReturnBadRequestWhenDateIsNull() throws Exception {
        // given
        ScannerRequest invalidRequest = ScannerRequest.builder()
                .scannerType("top_volume")
                .date(null)
                .count(10)
                .build();

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("無效請求 - 空的日期")
    void shouldReturnBadRequestWhenDateIsEmpty() throws Exception {
        // given
        ScannerRequest invalidRequest = ScannerRequest.builder()
                .scannerType("top_volume")
                .date("")
                .count(10)
                .build();

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("無效請求 - 數量為0")
    void shouldReturnBadRequestWhenCountIsZero() throws Exception {
        // given
        ScannerRequest invalidRequest = ScannerRequest.builder()
                .scannerType("top_volume")
                .date("2025-07-01")
                .count(0)
                .build();

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("無效請求 - 數量為負數")
    void shouldReturnBadRequestWhenCountIsNegative() throws Exception {
        // given
        ScannerRequest invalidRequest = ScannerRequest.builder()
                .scannerType("top_volume")
                .date("2025-07-01")
                .count(-5)
                .build();

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("無效請求 - 數量超過上限")
    void shouldReturnBadRequestWhenCountExceedsLimit() throws Exception {
        // given
        ScannerRequest invalidRequest = ScannerRequest.builder()
                .scannerType("top_volume")
                .date("2025-07-01")
                .count(250)
                .build();

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("服務異常 - 應該返回500狀態碼")
    void shouldReturnInternalServerErrorWhenServiceThrowsException() throws Exception {
        // given
        when(scannerService.getScannerData(any(ScannerRequest.class)))
                .thenThrow(new RuntimeException("Scanner service error"));

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("無效JSON - 應該返回400狀態碼")
    void shouldReturnBadRequestForInvalidJson() throws Exception {
        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("缺少Content-Type - 應該返回415狀態碼")
    void shouldReturnUnsupportedMediaTypeWithoutContentType() throws Exception {
        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("邊界值測試 - 數量為200")
    void shouldAcceptMaximumValidCount() throws Exception {
        // given
        ScannerRequest maxCountRequest = ScannerRequest.builder()
                .scannerType("top_volume")
                .date("2025-07-01")
                .count(200)
                .build();

        when(scannerService.getScannerData(any(ScannerRequest.class)))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maxCountRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("邊界值測試 - 數量為1")
    void shouldAcceptMinimumValidCount() throws Exception {
        // given
        ScannerRequest minCountRequest = ScannerRequest.builder()
                .scannerType("top_volume")
                .date("2025-07-01")
                .count(1)
                .build();

        when(scannerService.getScannerData(any(ScannerRequest.class)))
                .thenReturn(mockResponse);

        // when & then
        mockMvc.perform(post("/api/scanner/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minCountRequest)))
                .andExpect(status().isOk());
    }
}
