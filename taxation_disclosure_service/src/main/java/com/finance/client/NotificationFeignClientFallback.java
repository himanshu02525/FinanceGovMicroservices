package com.finance.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.finance.dto.NotificationRequestDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationFeignClientFallback implements NotificationFeignClient {

    @Override
    public ResponseEntity<Void> sendNotification(
            NotificationRequestDto request, String email) {

        log.warn("Notification service down. Skipping notification for entity {}",
                request.getEntityId());

        return ResponseEntity.ok().build();
    }
}
