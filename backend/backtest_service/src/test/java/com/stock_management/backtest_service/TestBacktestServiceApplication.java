package com.stock_management.backtest_service;

import org.springframework.boot.SpringApplication;

public class TestBacktestServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(BacktestServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
