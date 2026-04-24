package com.finance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.finance.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}