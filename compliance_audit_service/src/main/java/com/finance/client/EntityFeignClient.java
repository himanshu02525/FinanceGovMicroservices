package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "citizen-business-service")
public interface EntityFeignClient {
	@GetMapping("/entities/validate/{entityId}")
	Boolean validateEntity(@PathVariable("entityId") Long entityId);
}
