package com.finance.exceptions;

@SuppressWarnings("serial")
public class InvalidTaxStatusTransitionException extends RuntimeException {

    public InvalidTaxStatusTransitionException(String message) {
        super(message);
    }
}
