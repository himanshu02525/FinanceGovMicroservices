package com.finance.exceptions;

@SuppressWarnings("serial")
public class InvalidResourceStatusException extends RuntimeException {
	public InvalidResourceStatusException(String message) {
		super(message);
	}
}