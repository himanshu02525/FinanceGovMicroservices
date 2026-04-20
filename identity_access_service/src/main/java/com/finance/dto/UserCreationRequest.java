package com.finance.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {

    // For internal staff, we might want slightly longer usernames for clarity.
    @NotBlank(message = "An internal username is required.")
    @Size(min = 4, max = 25, message = "Username should be between 4 and 25 characters.")
    private String username;

    // This should ideally be an @gov.com or internal company email.
    @NotBlank(message = "An official email address is required.")
    @Email(message = "Please enter a valid official email.")
    private String email;

    // We require a stronger 10-character password for internal staff 
    // because they have access to sensitive financial data.
    @NotBlank(message = "A secure temporary password is required.")
    @Size(min = 10, message = "Staff passwords must be at least 10 characters.")
    private String password;

    // Unlike the public registration, here the Admin MUST pick a role 
    // (e.g., 'ADMIN', 'GOVERNMENT_AUDITOR', 'FINANCIAL_OFFICER').
    @NotBlank(message = "You must assign a specific role to this internal user.")
    private String role;
    

@NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;


	
}