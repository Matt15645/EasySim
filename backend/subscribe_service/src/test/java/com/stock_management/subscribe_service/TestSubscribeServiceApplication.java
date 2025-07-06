package com.stock_management.subscribe_service;

import org.springframework.boot.SpringApplication;

public class TestSubscribeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(SubscribeServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
