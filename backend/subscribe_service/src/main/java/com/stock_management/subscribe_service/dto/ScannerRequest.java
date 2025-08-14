package com.stock_management.subscribe_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScannerRequest {
    @JsonProperty("scanner_type")
    private String scannerType;     // Java 中使用駝峰命名
    
    private String date;            
    private int count = 100;        
    private boolean ascending = false;  
}
