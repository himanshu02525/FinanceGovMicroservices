package com.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private Long userId;      //
    private String token;     //
    private String message;   //
    private String role;      
    private String endpoint;  
}