package com.finance.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	// =====================================================
	// Entity Not Found (Citizen / Document)
	// =====================================================
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<String> handleResourceNotFound(EntityNotFoundException ex) {

		logger.error("Resource Not Found: {}", ex.getMessage());

		return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	// =====================================================
	// Validation Errors (@Valid)
	// =====================================================
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {

		String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();

		logger.warn("Validation Error: {}", errorMessage);

		return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
	}

	// =====================================================
	// Invalid JSON / Enum Values (DocType, Type)
	// =====================================================
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<String> handleInvalidJson(HttpMessageNotReadableException ex) {

		logger.error("Invalid JSON Input: {}", ex.getMessage());

		if (ex.getMessage().contains("DocType")) {
			return ResponseEntity.badRequest().body("Invalid DocType. Allowed values: PAN, AADHAR, PASSPORT, ID_PROOF");
		}

		if (ex.getMessage().contains("Type")) {
			return ResponseEntity.badRequest().body("Invalid Type. Allowed values: CITIZEN or BUSINESS");
		}

		return ResponseEntity.badRequest().body("Invalid request body or malformed JSON");
	}

	//
	// =====================================================
	// Generic / Unexpected Errors
	// =====================================================
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleGenericException(Exception ex) {

		logger.error("Unexpected Error Occurred", ex);

		return new ResponseEntity<>("Something went wrong. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
