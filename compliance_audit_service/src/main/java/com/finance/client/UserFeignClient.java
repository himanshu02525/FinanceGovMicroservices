package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.finance.dto.UserResponseDto;

@FeignClient(name = "identity-access-service")
public interface UserFeignClient {

	@GetMapping("officer/fetch/{entityId}")
	ResponseEntity<UserResponseDto> getOfficerById(@PathVariable Long officerId);
}
