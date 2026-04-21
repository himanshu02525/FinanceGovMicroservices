package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "taxation-disclosure-service")
public interface TaxClient {

	@GetMapping("/taxation/tax/summary")
	Map<String, Object> getTaxStatistics();
}
