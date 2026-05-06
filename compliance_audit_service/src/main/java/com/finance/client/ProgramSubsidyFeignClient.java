package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.finance.dto.FinancialProgramResponse;
import com.finance.dto.SubsidyResponse;
import com.finance.dto.SubsidyUpdateRequest;

@FeignClient(name = "financial-program-subsidy-service")
public interface ProgramSubsidyFeignClient {

	@GetMapping("/programs/fetch/{programId}")
	ResponseEntity<FinancialProgramResponse> getProgramById(@PathVariable Long programId);

	@GetMapping("/subsidies/fetch/{subsidyId}")
	ResponseEntity<SubsidyResponse> getSubsidyById(@PathVariable("subsidyId") Long subsidyId);

	@GetMapping("/subsidies/update/{subsidyId}")
	ResponseEntity<SubsidyResponse> updateSubsidy(@RequestBody SubsidyUpdateRequest requestBody,
			@PathVariable Long subsidyId);
}
