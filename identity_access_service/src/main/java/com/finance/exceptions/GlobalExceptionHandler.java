package com.finance.exceptions;


import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    /* ===============================
       COMMON JSON WRITER
       =============================== */
    private void writeSecurityError(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        response.setStatus(status.value());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), body);
    }

    /* ===============================
       401 — UNAUTHORIZED
       =============================== */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("Authentication failed: {}", authException.getMessage());

        writeSecurityError(
                response,
                HttpStatus.UNAUTHORIZED,
                "Authentication failed. Please login with valid credentials."
        );
    }

    /* ===============================
       403 — FORBIDDEN
       =============================== */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {

        log.warn("Access denied: {}", ex.getMessage());

        writeSecurityError(
                response,
                HttpStatus.FORBIDDEN,
                "You do not have permission to perform this action."
        );
    }

    /* ===============================
       LOGIN FAILURE
       =============================== */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(
                "Invalid email or password.",
                HttpStatus.UNAUTHORIZED
        );
    }

    /* ===============================
       USER NOT FOUND
       =============================== */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFound(UserNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /* ===============================
       VALIDATION ERRORS
       =============================== */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Validation failed");
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /* ===============================
       BUSINESS LOGIC ERRORS
       =============================== */
    @ExceptionHandler({ IllegalArgumentException.class, RuntimeException.class })
    public ResponseEntity<Object> handleBusinessExceptions(RuntimeException ex) {

        // ✅ IMPORTANT: return REAL message, not generic
        log.warn("Business rule violation: {}", ex.getMessage());

        return buildResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    /* ===============================
       FALLBACK — TRUE 500 ERROR
       =============================== */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUnhandled(Exception ex) {

        log.error("Unexpected system error", ex);

        return buildResponse(
                "Internal server error. Please contact support.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /* ===============================
       RESPONSE BUILDER
       =============================== */
    private ResponseEntity<Object> buildResponse(String message, HttpStatus status) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }
}
