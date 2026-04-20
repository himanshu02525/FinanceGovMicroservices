package com.finance.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpPasswordRequest {

    // The account we are trying to unlock
    private String email;

    // The 6-digit code we sent to their inbox
    private String otp;

    // The new password they want to set
    private String newPassword;
}