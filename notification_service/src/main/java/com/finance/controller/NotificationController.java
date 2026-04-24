package com.finance.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.NotificationRequestDto;
import com.finance.model.Notification;
import com.finance.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService service;

	@PostMapping("/trigger")
	public ResponseEntity<Void> send(@RequestBody NotificationRequestDto request, @RequestParam String email) {

		service.send(request, email);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/getAllNotification")
	public List<Notification> getAllNotifications() {
		return service.getAllNotifications();
	}

}