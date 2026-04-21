package com.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when a specific Tax Record ID does not exist.
 * Automatically maps to a 404 NOT FOUND response.
 */
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaxRecordNotFoundException extends RuntimeException {
    public TaxRecordNotFoundException(String message) {
        super(message);
    }
}