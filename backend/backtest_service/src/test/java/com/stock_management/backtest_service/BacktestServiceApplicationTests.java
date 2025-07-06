package com.stock_management.backtest_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BacktestServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
