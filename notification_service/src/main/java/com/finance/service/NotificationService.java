package com.finance.service;

import com.finance.dto.NotificationRequestDto;

public interface NotificationService {
	void send(NotificationRequestDto request, String email);
}