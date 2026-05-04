package com.finance.exceptions;

public class InvalidTaxStatusTransitionException extends RuntimeException {

    public InvalidTaxStatusTransitionException(String message) {
        super(message);
    }
}
