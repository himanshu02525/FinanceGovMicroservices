package com.finance.exceptions;

@SuppressWarnings("serial")
public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(Long id) {
        super("Application not found with ID: " + id);
    }
}