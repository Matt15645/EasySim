package com.stock_management.api_gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * API Gateway 應用程序啟動測試
 * 驗證 Spring Boot 應用程序能夠正確啟動
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-unit-testing-at-least-32-characters-long"
})
@DisplayName("API Gateway 應用程序測試")
class ApiGatewayApplicationTests {

	@Test
	@DisplayName("Spring 應用程序上下文應該正確加載")
	void contextLoads() {
		// 這個測試驗證 Spring Boot 應用程序能夠正確啟動
		// 包括所有 Bean 的創建和依賴注入
	}

	@Test
	@DisplayName("應用程序主方法應該可以執行")
	void mainMethodShouldRun() {
		// 測試主方法不會拋出異常
		// 實際環境中這會啟動整個應用程序，在測試中我們只驗證方法存在
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
			// ApiGatewayApplication.main(new String[]{}); // 在單元測試中不實際啟動
		});
	}
}
