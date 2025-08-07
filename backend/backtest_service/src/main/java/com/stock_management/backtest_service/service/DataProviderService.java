package com.stock_management.backtest_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataProviderService {

    @Value("${data.provider.url}")
    private String dataProviderUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 向 Data Provider 請求多支股票的歷史資料（使用non-blocking kbar API）
     */
    public Map<String, List<Map<String, Object>>> getHistoricalData(List<String> symbols, String startDate, String endDate) {
        try {
            // 建立請求體
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("symbols", symbols);
            requestBody.put("start_date", startDate);
            requestBody.put("end_date", endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 使用新的歷史資料端點
            String url = dataProviderUrl + "/api/historical";
            log.info("向 Data Provider 請求歷史資料: {}", url);
            log.debug("請求參數: symbols={}, start_date={}, end_date={}", symbols, startDate, endDate);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // 解析回應
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                Map<String, List<Map<String, Object>>> result = new HashMap<>();
                
                for (String symbol : symbols) {
                    List<Map<String, Object>> stockData = new ArrayList<>();
                    JsonNode symbolData = jsonNode.get(symbol);
                    
                    if (symbolData != null && symbolData.isArray()) {
                        for (JsonNode dataPoint : symbolData) {
                            Map<String, Object> point = new HashMap<>();
                            point.put("date", dataPoint.get("date").asText());
                            point.put("close", dataPoint.get("close").asDouble());
                            point.put("ts", dataPoint.get("ts").asLong());
                            stockData.add(point);
                        }
                    }
                    result.put(symbol, stockData);
                }
                
                log.info("成功從 Data Provider 取得資料，股票數量: {}", symbols.size());
                return result;
            } else {
                log.error("Data Provider 回應錯誤，狀態碼: {}", response.getStatusCode());
                throw new RuntimeException("Data Provider 回應錯誤: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("從 Data Provider 取得資料時發生錯誤: {}", e.getMessage(), e);
            throw new RuntimeException("取得歷史資料失敗: " + e.getMessage());
        }
    }
}
