package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.finance.dto.UserDto;

@FeignClient(name = "identity-access-service")
public interface UserFeignClient {
	
	@GetMapping("/api/users/getuserbyid/{id}")
	UserDto getUserById(@PathVariable("id") Long id);
}