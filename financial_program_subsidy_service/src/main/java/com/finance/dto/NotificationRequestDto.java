package com.finance.dto;
 
import com.finance.enums.NotificationCategory;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
 
/**
* Notification trigger contract.
* Used by OTHER microservices to call Notification Service via Feign.
*/
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
 
    /**
     * Receiver of the notification (User ID)
     * Example: Financial Officer, Citizen, Admin, etc.
     */
    private Long userId;
 
    /**
     * Related Citizen / Business entity ID
     * Used only for reference & audit
     */
    private Long entityId;
 
    /**
     * Notification message (shown in UI + email)
     */
    private String message;
 
    /**
     * Business context
     * Example: SUBSIDY, TAX, COMPLIANCE, GENERAL
     */
    private NotificationCategory category;
}