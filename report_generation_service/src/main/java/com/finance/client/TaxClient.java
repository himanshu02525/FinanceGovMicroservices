package com.finance.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "taxation-disclosure-service")
public interface TaxClient {

	@GetMapping("/api/taxation/tax/summary")
	Map<String, Object> getTaxStatistics(@RequestParam(required = false) Integer year);

}
