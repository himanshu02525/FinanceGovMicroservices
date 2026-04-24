package com.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SubsidyNotFoundException extends RuntimeException {
	public SubsidyNotFoundException(String message) {
		super(message);
	}
}
