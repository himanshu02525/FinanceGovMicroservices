package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.finance.dto.TaxResponseDTO;
import com.finance.dto.TaxUpdateDTO;

@FeignClient(name = "taxation-disclosure-service")
public interface TaxFeignClient {

	@GetMapping("api/taxation/taxerecords/{taxID}")
	ResponseEntity<TaxResponseDTO> getTaxById(@PathVariable Long taxID);

	@PutMapping("/api/taxation/taxrecords/verify/{taxId}")
	ResponseEntity<TaxResponseDTO> verifySingleTax(@PathVariable Long taxId, @RequestBody TaxUpdateDTO taxUpdateDTO);
}