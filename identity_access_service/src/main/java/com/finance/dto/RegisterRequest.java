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
public class RegisterRequest {

	// We need a unique username for the user's profile and identity.
	@NotBlank(message = "We need you to choose a username.")
	@Size(min = 4, max = 20, message = "Your username should be between 4 and 20 characters.")
	private String username;

	// The primary way we will contact the user and verify their account.
	@NotBlank(message = "An email address is required.")
	@Email(message = "That doesn't look like a valid email address.")
	private String email;

	// Financial apps require strong passwords to protect user data.
	@NotBlank(message = "New password is required")
	@Size(min = 8, message = "Password must be at least 8 characters long")
	@Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$", message = "Password must contain uppercase, lowercase, number, and special character")
	private String password;

	// Essential for Two-Factor Authentication (2FA) or urgent alerts.
	@NotBlank(message = "A phone number is required.")
	// This 'Regex' ensures the user only types exactly 10 numbers—no letters or
	// symbols.
	@Pattern(regexp = "^[0-9]{10}$", message = "Please enter a valid 10-digit phone number.")
	private String phone;

	// NOTE: We don't ask for 'Role' here.
	// For security, we should always default new public sign-ups to 'CITIZEN'
	// inside the Service layer so a hacker can't "register" as an ADMIN.
}