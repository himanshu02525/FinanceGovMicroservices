package com.finance.exceptions;

@SuppressWarnings("serial")
public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(String message) {
        super(message);
    }
}