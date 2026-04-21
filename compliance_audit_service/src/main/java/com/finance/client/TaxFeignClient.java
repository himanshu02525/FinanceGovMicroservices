package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.finance.dto.TaxResponseDTO;

@FeignClient(name = "taxation-disclosure-service")
public interface TaxFeignClient {

	@GetMapping("taxation/taxerecords/{taxID}")
	ResponseEntity<TaxResponseDTO> getTaxById(Long ref);
}