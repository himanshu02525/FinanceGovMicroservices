package com.finance.dto;
import com.finance.enums.NotificationCategory;
import lombok.Builder;
import lombok.Getter;
@Getter
@Builder
public class NotificationRequestDto {

    private Long userId;
    private Long entityId;
    private String message;
    private NotificationCategory category;
}