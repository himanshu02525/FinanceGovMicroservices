package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.finance.dto.FinancialProgramResponseDTO;

@FeignClient(name = "financial-program-subsidy-service")
public interface FinancialProgramClient {

	@GetMapping("/programs/fetch/{programId}")
	FinancialProgramResponseDTO getProgramById(@PathVariable Long programId);
}