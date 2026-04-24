package com.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Triggered when a user files for a year outside the allowed (Current/Previous) window
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTaxYearException extends RuntimeException {
    public InvalidTaxYearException(String message) {
        super(message);
    }
}