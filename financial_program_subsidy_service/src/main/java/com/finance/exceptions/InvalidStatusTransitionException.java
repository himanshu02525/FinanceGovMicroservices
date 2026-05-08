package com.finance.exceptions;

public class InvalidStatusTransitionException extends RuntimeException {
	public InvalidStatusTransitionException(String msg) {
		super(msg);
	}
}
