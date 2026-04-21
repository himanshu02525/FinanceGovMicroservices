package com.finance.service;

import java.util.List;

import com.finance.dto.NotificationRequestDto;
import com.finance.model.Notification;

public interface NotificationService {
	void send(NotificationRequestDto request, String email);
	
	List<Notification> getAllNotifications();
}