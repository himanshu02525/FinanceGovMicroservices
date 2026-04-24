package com.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.CONFLICT)
public class AuditStatusConflictException extends RuntimeException {
	public AuditStatusConflictException(String message) {
		super(message);
	}
}