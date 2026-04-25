package com.finance.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    // We check for @NotBlank because a string of just spaces is also "empty"
    @NotBlank(message = "We need your email address to log you in.")
    @Email(message = "That doesn't look like a valid email address.")
    private String email;

    @NotBlank(message = "Please enter your password.")
    // Security Best Practice: Don't allow passwords shorter than 8 characters
    @Size(min = 8, message = "Your password should be at least 8 characters.")
    private String password;

}