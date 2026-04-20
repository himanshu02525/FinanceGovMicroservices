package com.finance.client;

import java.util.List;

import com.finance.dto.UserDto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "identity-service")
public interface UserFeignClient {

	@GetMapping("/getAllUsers")
	List<UserDto> getAllUsers();
}