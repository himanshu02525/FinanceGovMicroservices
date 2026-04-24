package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "financial-program-subsidy-service")
public interface SubsidyClient {

	@GetMapping("/programs/summary")
	Map<String, Object> getProgramSummary();

	@GetMapping("/subsidies/summary")
	Map<String, Object> getSubsidySummary();
}