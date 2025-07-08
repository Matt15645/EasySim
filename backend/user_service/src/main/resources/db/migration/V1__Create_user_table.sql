CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,          -- 主鍵，自增 ID
    username VARCHAR(50) NOT NULL UNIQUE, -- 用戶名，唯一
    email VARCHAR(100) NOT NULL UNIQUE,   -- 電子郵件，唯一
    password VARCHAR(255) NOT NULL,       -- 密碼
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 創建時間
);

-- 新增索引
CREATE INDEX idx_users_email ON users (email); -- 電子郵件索引
CREATE INDEX idx_users_username ON users (username); -- 用戶名索引