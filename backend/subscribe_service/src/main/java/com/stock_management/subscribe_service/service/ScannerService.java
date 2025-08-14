package com.stock_management.subscribe_service.service;

import com.stock_management.subscribe_service.dto.ScannerRequest;
import com.stock_management.subscribe_service.dto.ScannerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ScannerService {
    
    @Value("${data.provider.url:http://localhost:8000}")
    private String dataProviderUrl;
    
    private final RestTemplate restTemplate;
    
    public ScannerService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5秒連接超時
        factory.setReadTimeout(30000);   // 30秒讀取超時
        this.restTemplate = new RestTemplate(factory);
    }
    
    public ScannerResponse getScannerData(ScannerRequest request) {
        try {
            // 構建請求 URL
            String url = dataProviderUrl + "/api/scanner";
            
            // 設定請求 headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 建立 HTTP 請求實體
            HttpEntity<ScannerRequest> httpEntity = new HttpEntity<>(request, headers);
            
            log.info("向 data-provider 請求掃描器資料: {}, 日期: {}, 數量: {}", 
                     request.getScannerType(), request.getDate(), request.getCount());
            
            // 呼叫 data-provider API
            ResponseEntity<Object> response = restTemplate.postForEntity(url, httpEntity, Object.class);
            
            log.info("收到 data-provider 回應，狀態碼: {}", response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = responseBody != null ? 
                    (List<Map<String, Object>>) responseBody.get("data") : null;
                
                log.info("成功取得掃描器資料，筆數: {}", data != null ? data.size() : 0);
                
                // 建立回應
                return ScannerResponse.builder()
                        .data(data)
                        .timestamp(java.time.LocalDateTime.now().toString())  // 轉為 String
                        .scannerType(request.getScannerType())
                        .date(request.getDate())
                        .count(data != null ? data.size() : 0)
                        .build();
            } else {
                throw new RuntimeException("data-provider 回應異常");
            }
            
        } catch (Exception e) {
            log.error("請求掃描器資料失敗: {}", e.getMessage());
            throw new RuntimeException("請求掃描器資料失敗: " + e.getMessage());
        }
    }
}
