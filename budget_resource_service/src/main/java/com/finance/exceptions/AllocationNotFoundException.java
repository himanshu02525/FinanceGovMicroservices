package com.finance.exceptions;

@SuppressWarnings("serial")
public class AllocationNotFoundException extends RuntimeException {
	public AllocationNotFoundException(String message) {
		super(message);
	}
}