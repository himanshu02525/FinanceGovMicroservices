package com.finance.exceptions;

@SuppressWarnings("serial")
public class ProgramNotFoundException extends RuntimeException {
    public ProgramNotFoundException(Long id) {
        super("Financial Program with ID " + id + " not found");
    }

	public ProgramNotFoundException(String string) {

	}
}
