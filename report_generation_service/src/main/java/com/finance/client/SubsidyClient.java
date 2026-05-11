package com.finance.client;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "financial-program-subsidy-service")
public interface SubsidyClient {

	@GetMapping("/subsidies/summary")
	Map<String, Object> getSubsidySummary();

	@GetMapping("/programs/summary")
	Map<String, Object> getProgramSummary(@RequestParam("id") Long id);
}