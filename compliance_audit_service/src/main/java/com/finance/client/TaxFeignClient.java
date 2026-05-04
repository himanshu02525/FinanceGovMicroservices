package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.finance.dto.TaxResponseDTO;

@FeignClient(name = "taxation-disclosure-service")
public interface TaxFeignClient {

	@GetMapping("/api/taxation/taxrecords/{taxId}")
	TaxResponseDTO getTaxById(@PathVariable Long taxId);
}