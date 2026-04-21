package com.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for Identity & Access Management.
 * Ensures that failed lookups for Citizens or Officers return a 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    // Standard constructor for a simple message
    public UserNotFoundException(String message) {
        super(message);
    }
    
    // Overloaded constructor for professional error reporting.
    // Example: throw new UserNotFoundException("admin@gov.com", "Officer");
    // Result: "Officer not found with identifier: admin@gov.com"
    public UserNotFoundException(String identifier, String type) {
        super(String.format("%s not found with identifier: %s", type, identifier));
    }
}