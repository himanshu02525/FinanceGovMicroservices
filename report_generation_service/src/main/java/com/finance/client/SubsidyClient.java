package com.finance.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "financial-program-subsidy-service")
public interface SubsidyClient {

	@GetMapping("/programs/summary")
	Map<String, Object> getProgramSummary();

	@GetMapping("/subsidies/summary")
	Map<String, Object> getSubsidySummary();

	@GetMapping("/programs/summary/{programId}")
	Map<String, Object> getProgramSummary(@PathVariable Long programId);
}