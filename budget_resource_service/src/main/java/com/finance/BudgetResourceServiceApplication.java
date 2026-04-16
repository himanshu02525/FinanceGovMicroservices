package com.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BudgetResourceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetResourceServiceApplication.class, args);
	}

}
