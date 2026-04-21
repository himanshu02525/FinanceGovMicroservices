package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.finance.dto.FinancialProgramResponse;
import com.finance.dto.SubsidyResponse;

@FeignClient(name = "financial-program-subsidy-service")
public interface ProgramSubsidyFeignClient {

	@GetMapping("/programs/fetch/{programId}")
	ResponseEntity<FinancialProgramResponse> getProgramById(Long ref);

	@GetMapping("/subsidies/fetch/{subsidyId}")
	ResponseEntity<SubsidyResponse> getSubsidyById(Long ref);
}