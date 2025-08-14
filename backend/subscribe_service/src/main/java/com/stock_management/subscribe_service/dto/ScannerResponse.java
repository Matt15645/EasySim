package com.stock_management.subscribe_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScannerResponse {
    private List<Map<String, Object>> data;    // 掃描器結果資料
    private String timestamp;                  // 資料時間戳（改為 String）
    private String scannerType;                // 掃描器類型
    private String date;                       // 查詢日期
    private int count;                         // 實際回傳數量
}
