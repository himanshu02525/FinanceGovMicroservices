package com.finance.exceptions;

@SuppressWarnings("serial")
public class InvalidAllocationStatusException extends RuntimeException {
	public InvalidAllocationStatusException(String message) {
		super(message);
	}
}