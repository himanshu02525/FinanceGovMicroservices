package com.finance.dto;

import com.finance.enums.NotificationCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor          // ✅ REQUIRED for Jackson deserialization
@AllArgsConstructor         // ✅ Used by Builder and frameworks
public class NotificationRequestDto {

    private Long userId;
    private Long entityId;
    private String message;
    private NotificationCategory category;
}