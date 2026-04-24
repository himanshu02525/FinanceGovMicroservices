package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.finance.dto.NotificationRequestDto;

@FeignClient(name = "notification-service")
public interface NotificationFeignClient {

	@PostMapping("/api/notifications")
	void sendNotification(@RequestBody NotificationRequestDto request, @RequestParam String email);
}