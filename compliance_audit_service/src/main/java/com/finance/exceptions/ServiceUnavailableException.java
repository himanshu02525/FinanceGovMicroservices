package com.finance.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.NoArgsConstructor;

@ResponseStatus(code = HttpStatus.SERVICE_UNAVAILABLE)
@NoArgsConstructor
public class ServiceUnavailableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ServiceUnavailableException(String msg) {
		super(msg);
	}
}
