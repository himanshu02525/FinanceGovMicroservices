package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.finance.dto.TaxResponseDTO;

@FeignClient(name = "taxation-disclosure-service")
public interface TaxFeignClient {

	@GetMapping("/api/taxation/taxrecords/{id}")
	ResponseEntity<TaxResponseDTO> getTaxById(@PathVariable Long id);
}