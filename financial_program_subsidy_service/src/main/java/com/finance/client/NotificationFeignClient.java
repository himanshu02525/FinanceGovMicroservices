package com.finance.client;
 
import com.finance.dto.NotificationRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
 
@FeignClient(
    name = "notification-service",
    url = "${notification.service.url}"
)
public interface NotificationFeignClient {
 
    @PostMapping("/api/notifications")
    void sendNotification(
        @RequestBody NotificationRequestDto request,
        @RequestParam("email") String email
    );
}