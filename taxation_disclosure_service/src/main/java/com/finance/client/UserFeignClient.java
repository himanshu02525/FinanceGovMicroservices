package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.finance.dto.UserDto;

/**
 * Feign client for Identity/User microservice.
 */
@FeignClient(name = "identity-service")
public interface UserFeignClient {

	/**
	 * Fetch a single user by ID from Identity Service
	 */
	@GetMapping("/api/users/getuserbyid/{id}")
	UserDto getUserById(@PathVariable("id") Long id);
}
