package com.finance.exceptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	/*
	 * ========================================================== VALIDATION
	 * EXCEPTIONS ==========================================================
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {

		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + " : " + err.getDefaultMessage()).collect(Collectors.toList());

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("message", "Validation failed");
		body.put("errors", errors);

		return ResponseEntity.badRequest().body(body);
	}

	/*
	 * ========================================================== JSON PARSING /
	 * JACKSON ERRORS ==========================================================
	 */
	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<Object> invalidFormat(InvalidFormatException ex) {
		return buildResponse("Invalid input format: " + ex.getOriginalMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Object> jsonNotReadable(HttpMessageNotReadableException ex) {
		return buildResponse("Malformed JSON request or invalid data type", HttpStatus.BAD_REQUEST);
	}

	/*
	 * ========================================================== AUDIT MODULE
	 * ==========================================================
	 */
	@ExceptionHandler(AuditRecordNotFoundException.class)
	public ResponseEntity<ExceptionResponse> auditRecordNotFound(AuditRecordNotFoundException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(AuditStatusConflictException.class)
	public ResponseEntity<ExceptionResponse> auditStatusConflict(AuditStatusConflictException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}

	/*
	 * ========================================================== COMPLIANCE MODULE
	 * ==========================================================
	 */
	@ExceptionHandler(ComplianceNotFoundException.class)
	public ResponseEntity<ExceptionResponse> complianceNotFound(ComplianceNotFoundException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ComplianceStatusConflictException.class)
	public ResponseEntity<ExceptionResponse> complianceStatusConflict(ComplianceStatusConflictException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(ComplianceViolationException.class)
	public ResponseEntity<ExceptionResponse> complianceViolation(ComplianceViolationException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	/*
	 * ========================================================== BUSINESS
	 * EXCEPTIONS (Runtime)
	 * ==========================================================
	 */
	@ExceptionHandler({ IllegalArgumentException.class, RuntimeException.class })
	public ResponseEntity<Object> handleBusinessExceptions(RuntimeException ex) {
		log.warn("Business error: {}", ex.getMessage());
		return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	/*
	 * ========================================================== GLOBAL FALLBACK
	 * EXCEPTION ==========================================================
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleAllUnhandled(Exception ex) {

		log.error("Unexpected system error", ex);

		return buildResponse("Internal server error. Please contact support.", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/*
	 * ========================================================== RESPONSE BUILDERS
	 * ==========================================================
	 */
	private ResponseEntity<Object> buildResponse(String message, HttpStatus status) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", status.value());
		body.put("message", message);
		return new ResponseEntity<>(body, status);
	}

	private ResponseEntity<ExceptionResponse> buildExceptionResponse(String message, HttpStatus status) {
		ExceptionResponse exception = new ExceptionResponse(message, LocalDate.now(), status.value());
		return new ResponseEntity<>(exception, status);
	}

}