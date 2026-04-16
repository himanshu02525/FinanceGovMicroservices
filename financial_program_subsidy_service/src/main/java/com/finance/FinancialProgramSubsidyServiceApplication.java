package com.finance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FinancialProgramSubsidyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialProgramSubsidyServiceApplication.class, args);
	}

}
