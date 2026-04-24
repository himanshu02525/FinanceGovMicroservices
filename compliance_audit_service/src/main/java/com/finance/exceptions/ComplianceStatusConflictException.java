package com.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.CONFLICT)
public class ComplianceStatusConflictException extends RuntimeException {
	public ComplianceStatusConflictException(String message) {
		super(message);
	}
}