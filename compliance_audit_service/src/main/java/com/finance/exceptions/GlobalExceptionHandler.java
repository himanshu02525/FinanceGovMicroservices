package com.finance.exceptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.finance.util.MessageUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
	private final MessageUtil messageUtil;

	/*
	 * ========================= VALIDATION EXCEPTIONS =========================
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {

		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + " : " + err.getDefaultMessage()).toList();

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("message", "Validation failed");
		body.put("errors", errors);

		return ResponseEntity.badRequest().body(body);
	}

	/* ========================= JSON ========================= */
	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<Object> handleInvalidFormat(InvalidFormatException ex) {
		return buildGenericResponse("Invalid input format: " + ex.getOriginalMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Object> handleJsonNotReadable(HttpMessageNotReadableException ex) {
		return buildGenericResponse("Malformed JSON request or invalid data type", HttpStatus.BAD_REQUEST);
	}

	/*
	 * ========================= AUDIT MODULE =========================
	 */
	@ExceptionHandler(AuditRecordNotFoundException.class)
	public ResponseEntity<ExceptionResponse> handleAuditRecordNotFound(AuditRecordNotFoundException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(AuditStatusConflictException.class)
	public ResponseEntity<ExceptionResponse> handleAuditStatusConflict(AuditStatusConflictException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}

	/*
	 * ========================= COMPLIANCE MODULE =========================
	 */
	@ExceptionHandler(ComplianceNotFoundException.class)
	public ResponseEntity<ExceptionResponse> handleComplianceNotFound(ComplianceNotFoundException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ComplianceStatusConflictException.class)
	public ResponseEntity<ExceptionResponse> handleComplianceStatusConflict(ComplianceStatusConflictException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}

	@ExceptionHandler(ComplianceViolationException.class)
	public ResponseEntity<ExceptionResponse> handleComplianceViolation(ComplianceViolationException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	/*
	 * ========================= GENERIC NOT FOUND EXCEPTIONS
	 * =========================
	 */
	@ExceptionHandler({ EntityNotFoundException.class, ProgramNotFoundException.class, SubsidyNotFoundException.class,
			TaxRecordNotFoundException.class, UserNotFoundException.class })
	public ResponseEntity<ExceptionResponse> handleNotFoundExceptions(RuntimeException ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	/*
	 * ========================= BUSINESS / RUNTIME EXCEPTIONS
	 * =========================
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
		log.warn("Illegal argument: {}", ex.getMessage());
		return buildGenericResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
		log.warn("Runtime business exception: {}", ex.getMessage());
		return buildGenericResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
	}

	/*
	 * ========================= Feign Clients EXCEPTIONS =========================
	 */
	@ExceptionHandler(feign.FeignException.NotFound.class)
	public ResponseEntity<ExceptionResponse> handleFeignNotFound(feign.FeignException.NotFound ex) {
		return buildExceptionResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(feign.FeignException.class)
	public ResponseEntity<ExceptionResponse> handleFeignError(feign.FeignException ex) {

		String messageKey;

		if (ex.status() == 404) {
			messageKey = "external.service.not.found";
		} else {
			messageKey = "external.service.unavailable";
		}

		String message = messageUtil.getMessage(messageKey);

		return buildExceptionResponse(message, HttpStatus.BAD_GATEWAY);
	}

	/*
	 * ========================= GLOBAL FALLBACK EXCEPTION =========================
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleUnhandledException(Exception ex) {
		log.error("Unexpected system error", ex);
		return buildGenericResponse("Internal server error. Please contact support.", HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/*
	 * ========================= RESPONSE BUILDERS =========================
	 */
	private ResponseEntity<Object> buildGenericResponse(String message, HttpStatus status) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("message", message);
		body.put("timestamp", LocalDateTime.now());
		body.put("status", status.value());

		return new ResponseEntity<>(body, status);
	}

	private ResponseEntity<ExceptionResponse> buildExceptionResponse(String message, HttpStatus status) {
		ExceptionResponse response = new ExceptionResponse(message, LocalDate.now(), status.value());
		return new ResponseEntity<>(response, status);
	}
}