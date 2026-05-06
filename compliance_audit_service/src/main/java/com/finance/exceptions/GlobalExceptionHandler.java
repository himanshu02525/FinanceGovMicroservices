package com.finance.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	private static final String MESSAGE = "message";
	/* ================= NOT FOUND EXCEPTIONS (404) ================= */

	@ExceptionHandler({ AuditRecordNotFoundException.class, ComplianceNotFoundException.class,
			UserNotFoundException.class, EntityNotFoundException.class, ProgramNotFoundException.class,
			SubsidyNotFoundException.class, TaxRecordNotFoundException.class })
	public ResponseEntity<Object> handleNotFoundExceptions(RuntimeException ex, WebRequest request) {
		log.error("Resource not found: {}", ex.getMessage());
		return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
	}

	/*
	 * ================= CONFLICT / BUSINESS LOGIC EXCEPTIONS (409)
	 * =================
	 */

	@ExceptionHandler({ AuditStatusConflictException.class, ComplianceStatusConflictException.class })
	public ResponseEntity<Object> handleConflictExceptions(RuntimeException ex, WebRequest request) {
		log.error("Conflict in record state: {}", ex.getMessage());
		return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}

	/* ================= FEIGN CLIENT EXCEPTIONS ================= */
	/* ================= SERVICE UNAVAILABLE EXCEPTIONS (503) ================= */

	@ExceptionHandler(ServiceUnavailableException.class)
	public ResponseEntity<Object> handleServiceUnavailable(ServiceUnavailableException ex) {
		log.error("Service outage: {}", ex.getMessage());
		// This will now return your clean JSON instead of the stack trace
		return buildResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
	}

	/*
	 * ================= FEIGN CLIENT EXCEPTIONS (For Raw Feign Errors)
	 * =================
	 */

	@ExceptionHandler(FeignException.class)
	public ResponseEntity<Object> handleRawFeignException(FeignException ex) {
		log.error("Unhandled Feign client error: Status {}, Message: {}", ex.status(), ex.getMessage());

		String responseBody = ex.contentUTF8();
		String friendlyMessage = "Remote service error";

		if (responseBody != null && !responseBody.isEmpty()) {
			try {
				JsonNode node = new ObjectMapper().readTree(responseBody);
				friendlyMessage = node.has("message") ? node.get("message").asText() : responseBody;
			} catch (Exception e) {
				friendlyMessage = responseBody;
			}
		}

		HttpStatus status = HttpStatus.resolve(ex.status());
		return buildResponse(friendlyMessage, status != null ? status : HttpStatus.SERVICE_UNAVAILABLE);
	}

	/* ================= GENERIC FALLBACK (500) ================= */

//	@ExceptionHandler(Exception.class)
//	public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
//		log.error("Internal Server Error: ", ex);
//		return buildResponse("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
//	}

	/* ================= UTILITY METHOD ================= */

	private ResponseEntity<Object> buildResponse(String message, HttpStatus status) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put(MESSAGE, message);
		return new ResponseEntity<>(body, status);

	}
}