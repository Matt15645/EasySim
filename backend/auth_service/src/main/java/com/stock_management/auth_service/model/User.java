package com.stock_management.auth_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity  // 標記為 JPA 實體
@Table(name = "users")  // 指定資料表名稱為 "users"
@Data    // Lombok: 自動生成 getter、setter、toString 等方法
@NoArgsConstructor  // Lombok: 無參數建構函式
@AllArgsConstructor // Lombok: 全參數建構函式
@Builder  // Lombok: 建造者模式
public class User {
    
    @Id  // 主鍵標記
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 使用資料庫的自動增長機制
    private Long id;
    
    @Column(nullable = false, unique = true)  // 不能為空，且必須唯一
    private String username;
    
    @Column(nullable = false, unique = true)  // 不能為空，且必須唯一
    private String email;
    
    @Column(nullable = false)  // 不能為空
    private String password;  // 將儲存加密後的密碼
    
    @Column(name = "created_at")  // 自定義資料表欄位名稱
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")  // 自定義資料表欄位名稱
    private LocalDateTime updatedAt;
    
    @PrePersist  // 在實體被持久化之前自動呼叫
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();  // 設定創建時間
        this.updatedAt = LocalDateTime.now();  // 設定更新時間
    }
    
    @PreUpdate  // 在實體被更新之前自動呼叫
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();  // 更新更新時間
    }
}