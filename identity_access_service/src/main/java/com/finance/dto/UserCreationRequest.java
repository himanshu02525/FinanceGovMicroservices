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

    // Official / internal email
    @NotBlank(message = "An official email address is required.")
    @Email(message = "Please enter a valid official email.")
    private String email;

    // ✅ STRONG PASSWORD VALIDATION (ENTERPRISE GRADE)
    @NotBlank(message = "A secure temporary password is required.")
    @Size(min = 10, message = "Staff passwords must be at least 10 characters long.")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character (@#$%^&+=!)"
    )
    private String password;

    // Admin must explicitly assign a role
    @NotBlank(message = "You must assign a specific role to this internal user.")
    private String role;

    // ✅ Phone validation (10 digits only)
    @NotBlank(message = "Phone number is required.")
    @Pattern(
        regexp = "^[0-9]{10}$",
        message = "Phone number must be exactly 10 digits."
    )
    private String phone;
}
