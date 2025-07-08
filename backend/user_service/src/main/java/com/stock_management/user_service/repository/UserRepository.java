package com.stock_management.user_service.repository;

import com.stock_management.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository  // 標記為 Spring 存儲庫
public interface UserRepository extends JpaRepository<User, Long> {  // 泛型參數: <實體類型, 主鍵類型>
    
    // 根據電子郵件查找用戶
    Optional<User> findByEmail(String email);  // 返回 Optional 避免空指針異常
    
    // 根據用戶名查找用戶
    Optional<User> findByUsername(String username);
    
    // 檢查電子郵件是否已存在
    boolean existsByEmail(String email);
    
    // 檢查用戶名是否已存在
    boolean existsByUsername(String username);
}