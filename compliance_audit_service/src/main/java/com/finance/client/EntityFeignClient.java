package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.finance.dto.CitizenBusiness;

@FeignClient(name = "citizen-business-service")
public interface EntityFeignClient {

	@GetMapping("/entities/getCitizenById/{id}")
	ResponseEntity<CitizenBusiness> getCitizenById(@PathVariable("id") Long id);
}
