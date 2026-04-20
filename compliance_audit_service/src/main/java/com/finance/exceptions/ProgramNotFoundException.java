package com.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProgramNotFoundException extends RuntimeException {
	public ProgramNotFoundException(Long id) {
		super("Financial Program with ID " + id + " not found");
	}

	public ProgramNotFoundException(String string) {

	}
}
