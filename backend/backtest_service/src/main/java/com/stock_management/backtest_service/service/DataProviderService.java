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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
     * 向 Data Provider 請求多支股票的歷史資料
     */
    public Map<String, List<Map<String, Object>>> getHistoricalData(List<String> symbols, String startDate, String endDate) {
        try {
            // 生成日期範圍
            List<String> dates = generateDateRange(startDate, endDate);
            
            // 建立請求體
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("symbols", symbols);
            requestBody.put("dates", dates);

            // 設定 HTTP 標頭
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 建立 HTTP 實體
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 發送 POST 請求到 Data Provider
            String url = dataProviderUrl + "/api/ticks";
            log.info("向 Data Provider 請求資料: {}", url);
            log.debug("請求參數: symbols={}, dates={}", symbols, dates);

            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.POST, 
                entity, 
                String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // 解析 JSON 回應
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

        } catch (ResourceAccessException e) {
            log.error("無法連接到 Data Provider: {}", e.getMessage());
            throw new RuntimeException("無法連接到 Data Provider: " + e.getMessage());
        } catch (Exception e) {
            log.error("從 Data Provider 取得資料時發生錯誤: {}", e.getMessage());
            throw new RuntimeException("取得歷史資料失敗: " + e.getMessage());
        }
    }

    /**
     * 生成日期範圍
     */
    private List<String> generateDateRange(String startDate, String endDate) {
        List<String> dates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current.format(formatter));
            current = current.plusDays(1);
        }
        
        return dates;
    }
}
