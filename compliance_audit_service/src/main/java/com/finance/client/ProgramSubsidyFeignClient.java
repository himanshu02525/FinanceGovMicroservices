package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.finance.dto.FinancialProgramResponse;
import com.finance.dto.SubsidyResponse;

@FeignClient(name = "financial-program-subsidy-service")
public interface ProgramSubsidyFeignClient {

	@GetMapping("/programs/fetch/{programId}")
	FinancialProgramResponse getProgramById(@PathVariable Long programId);

	@GetMapping("/subsidies/fetch/{subsidyId}")
	SubsidyResponse getSubsidyById(@PathVariable Long subsidyId);
}
