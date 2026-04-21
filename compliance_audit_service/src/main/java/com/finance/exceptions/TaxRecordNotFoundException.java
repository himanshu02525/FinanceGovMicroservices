package com.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaxRecordNotFoundException extends RuntimeException {

	public TaxRecordNotFoundException(String message) {
		super(message);
	}
}