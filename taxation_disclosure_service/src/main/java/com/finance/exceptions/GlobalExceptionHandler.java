package com.finance.exceptions;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import lombok.AllArgsConstructor;
import lombok.Data;

@RestControllerAdvice // Centralized exception handling for the Finance microservices
public class GlobalExceptionHandler {

	// 1. Handle Resource Not Found (Generic catch for missing DB entries)
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	// 2. NEW: Specific handle for Tax Record lookup failures
	@ExceptionHandler(TaxRecordNotFoundException.class)
	public ResponseEntity<Object> handleTaxRecordNotFoundException(TaxRecordNotFoundException ex, WebRequest request) {
		// Returns 404 with the specific message "Tax record with ID X not found"
		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	// 3. Handle Entity Not Found (e.g., Citizen ID verification failure via Feign)
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	// 4. Handle Invalid Tax Year (e.g., filing for years outside current/previous
	// range)
	@ExceptionHandler(InvalidTaxYearException.class)
	public ResponseEntity<Object> handleInvalidTaxYearException(InvalidTaxYearException ex, WebRequest request) {
		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
	}

	// 5. Handle DTO Validation Errors (e.g., @NotNull, @Min)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatus.BAD_REQUEST.value());

		// Extract the first validation message found in the BindingResult
		String message = ex.getBindingResult().getFieldErrors().stream().map(error -> error.getDefaultMessage())
				.findFirst().orElse("Validation Failed");

		body.put("message", "Validation Failed: " + message);

		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	// 6. Global Fallback (Catch-all for 500 Internal Server Errors)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleGeneralException(Exception ex, WebRequest request) {
		// Logs the actual stack trace for developers while hiding details from users
		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error occurred", request);
	}

	// Centralized helper to build the JSON error body
	private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message, WebRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now()); // Time of failure
		body.put("status", status.value()); // 404, 400, 500 etc.
		body.put("error", status.getReasonPhrase()); // e.g., "Not Found"
		body.put("message", message); // Custom message from the exception
		body.put("path", request.getDescription(false)); // Endpoint URI that was called

		return new ResponseEntity<>(body, status);
	}

	/**
	 * Inner class for structured error responses (can be used instead of Map if
	 * preferred)
	 */
	@Data
	@AllArgsConstructor
	public static class ErrorResponse {
		private LocalDateTime timestamp;
		private int status;
		private String message;
		private String details;
	}
}