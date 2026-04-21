package com.finance.service;

import java.util.List;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.dto.NotificationRequestDto;
import com.finance.enums.NotificationStatus;
import com.finance.model.Notification;
import com.finance.repository.NotificationRepository;
import com.finance.util.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository repository;
	private final EmailService emailService;

	@Override
	public void send(NotificationRequestDto request, String email) {

		Notification notification = new Notification();
		notification.setUserId(request.getUserId());
		notification.setEntityId(request.getEntityId());
		notification.setMessage(request.getMessage());
		notification.setCategory(request.getCategory());

		repository.save(notification);

		try {
			emailService.send(email, "FinanceGov Notification", request.getMessage());
			notification.setStatus(NotificationStatus.SENT);
		} catch (MailException ex) {
			notification.setStatus(NotificationStatus.FAILED);
		}

		repository.save(notification);
	}

	@Override
	public List<Notification> getAllNotifications() {
		return repository.findAll();
	}

}