package com.finance.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// =====================================================
	// Report Not Found Exception
	// =====================================================
	@ExceptionHandler(ReportNotFoundException.class)
	public ResponseEntity<String> handleReportNotFound(ReportNotFoundException ex) {

		logger.error("Report not found: {}", ex.getMessage());

		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
	}
	//

	// =====================================================
	// Invalid Path Variable 
	// =====================================================
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<String> handleMethodArgumentMismatch(MethodArgumentTypeMismatchException ex) {

		logger.error("Invalid path variable value: {}", ex.getValue());

		return new ResponseEntity<>("Invalid report scope. Allowed values: PROGRAM, SUBSIDY, TAX, COMPLIANCE",
				HttpStatus.BAD_REQUEST);
	}

	// =====================================================
	// Invalid JSON Input
	// =====================================================
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<String> handleInvalidJson(HttpMessageNotReadableException ex) {

		logger.error("Invalid JSON request body", ex);

		return new ResponseEntity<>("Invalid request body or malformed JSON", HttpStatus.BAD_REQUEST);
	}

	// =====================================================
	// Generic / Unexpected Exception
	// =====================================================
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleGenericException(Exception ex) {

		logger.error("Unexpected error occurred", ex);

		return new ResponseEntity<>("Something went wrong while processing the request",
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}