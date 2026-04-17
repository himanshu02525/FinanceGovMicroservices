package com.finance.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.finance.dto.FinancialProgramResponseDTO;

@FeignClient(name = "financial-program-subsidy-service")
@Profile("dev")
public interface FinancialProgramClient {

	@GetMapping("/programs/fetch/{programId}")
	FinancialProgramResponseDTO getProgramById(@PathVariable Long programId);

	@GetMapping("/subsidies/approved_sum/{programId}")
	BigDecimal getApprovedAmount(@PathVariable Long programId);

}