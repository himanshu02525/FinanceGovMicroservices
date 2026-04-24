package com.finance.exceptions;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private Map<String, Object> buildBody(HttpStatus status, String message, String errorCode, String path) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", OffsetDateTime.now().toString());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", message);
		body.put("errorCode", errorCode);
		body.put("path", path);
		return body;
	}

	private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message, String errorCode,
			HttpServletRequest request) {

		return ResponseEntity.status(status).body(buildBody(status, message, errorCode, request.getRequestURI()));
	}

	/*
	 * ========================================================== VALIDATION
	 * EXCEPTIONS ==========================================================
	 */

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		List<Map<String, String>> details = new ArrayList<>();

		ex.getBindingResult().getFieldErrors().forEach(error -> {
			Map<String, String> err = new HashMap<>();
			err.put("field", error.getField());
			err.put("message", error.getDefaultMessage());
			details.add(err);
		});

		log.warn("DTO validation failed at {}", request.getRequestURI());

		Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Validation failed. Please review the request.",
				"VALIDATION_ERROR", request.getRequestURI());

		body.put("details", details);

		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex,
			HttpServletRequest request) {

		String message = ex.getConstraintViolations().iterator().next().getMessage();

		log.warn("Constraint violation at {} : {}", request.getRequestURI(), message);

		return buildResponse(HttpStatus.BAD_REQUEST, message, "VALIDATION_ERROR", request);
	}

	/*
	 * ========================================================== JSON / PARSING
	 * ERRORS ==========================================================
	 */

	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidFormat(InvalidFormatException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, "Invalid input format: " + ex.getOriginalMessage(),
				"INVALID_JSON_FORMAT", request);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleUnreadableJson(HttpMessageNotReadableException ex,
			HttpServletRequest request) {

		return buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request or invalid data type.", "MALFORMED_JSON",
				request);
	}

	/*
	 * ========================================================== BUSINESS / DOMAIN
	 * EXCEPTIONS ==========================================================
	 */

	@ExceptionHandler({ InvalidAllocationStatusException.class, InvalidResourceStatusException.class,
			IllegalStateException.class, IllegalArgumentException.class })
	public ResponseEntity<Map<String, Object>> handleBusinessExceptions(RuntimeException ex,
			HttpServletRequest request) {

		log.warn("Business rule violation at {} : {}", request.getRequestURI(), ex.getMessage());

		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "BUSINESS_RULE_VIOLATION", request);
	}

	/*
	 * ========================================================== NOT FOUND
	 * EXCEPTIONS ==========================================================
	 */

	@ExceptionHandler({ ProgramNotFound.class, AllocationNotFoundException.class, ResourceNotFoundException.class })
	public ResponseEntity<Map<String, Object>> handleNotFoundExceptions(RuntimeException ex,
			HttpServletRequest request) {

		log.warn("Resource not found at {} : {}", request.getRequestURI(), ex.getMessage());

		return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "Issue With The PROGRAM_ID", request);
	}

	/*
	 * ========================================================== FALLBACK (500)
	 * ==========================================================
	 */

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleAllUnhandled(Exception ex, HttpServletRequest request) {

		log.error("Unhandled exception at {}", request.getRequestURI(), ex);

		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occured please see the logs for more info ",
				"UNEXPECTED_ERROR", request);
	}
}