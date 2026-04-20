package com.finance.exceptions;

import java.time.LocalDate;

public class ExceptionResponse {
	private String errorMessage;
	private LocalDate dateOfException;
	private int statecode;

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public LocalDate getDateOfException() {
		return dateOfException;
	}

	public void setDateOfException(LocalDate dateOfException) {
		this.dateOfException = dateOfException;
	}

	public int getStatecode() {
		return statecode;
	}

	public void setStatecode(int statecode) {
		this.statecode = statecode;
	}

	public ExceptionResponse(String errorMessage, LocalDate dateOfException, int statecode) {
		super();
		this.errorMessage = errorMessage;
		this.dateOfException = dateOfException;
		this.statecode = statecode;
	}


}
