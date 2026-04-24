package com.finance.service;

import com.finance.dto.AuthResponse;
import com.finance.dto.LoginRequest;
import com.finance.dto.RegisterRequest;

/**
 * The Security Gateway for the FinanceGov System.
 * This interface defines how we onboard new citizens and verify existing officers.
 */
public interface AuthService {

    /**
     * Handles new user registration.
     * Implementation should automatically assign 'ROLE_CITIZEN' to ensure 
     * that public sign-ups don't accidentally get administrative powers.
     */
    String register(RegisterRequest request);

    /**
     * Authenticates credentials and issues a secure JWT.
     * It also determines the 'AuthResponse' which includes the role-based 
     * redirect path (e.g., /admin/dashboard or /citizen/home).
     */
    AuthResponse login(LoginRequest request);
}