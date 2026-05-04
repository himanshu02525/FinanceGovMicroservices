package com.finance.exceptions;

@SuppressWarnings("serial")
public class NoSubsidiesFoundException extends RuntimeException {
    public NoSubsidiesFoundException() {
        super("No subsidies available in the system.");
    }
}
