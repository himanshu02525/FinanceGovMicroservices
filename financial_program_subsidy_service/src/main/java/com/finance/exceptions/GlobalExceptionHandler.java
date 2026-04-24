package com.finance.exceptions;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ProgramNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleProgramNotFound(ProgramNotFoundException ex) {
	    Map<String, Object> error = new HashMap<>();
	    error.put("timestamp", Instant.now());
	    error.put("status", HttpStatus.NOT_FOUND.value());
	    error.put("error", "Program Not Found");
	    error.put("message", ex.getMessage());
	    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolation(
	        ConstraintViolationException ex) {

	    Map<String, Object> error = new HashMap<>();
	    error.put("timestamp", LocalDateTime.now());
	    error.put("status", HttpStatus.BAD_REQUEST.value());
	    error.put("error", "Bad Request");

	    // ✅ Extract ONLY the validation messages
	    String message = ex.getConstraintViolations()
	            .iterator()
	            .next()
	            .getMessage();

	    error.put("message", message);

	    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	}

	

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
		Map<String, Object> error = new HashMap<>();
		error.put("timestamp", LocalDateTime.now());
		error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		error.put("error", "Internal Server Error");
		error.put("message", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);  //HTTP 500
	}
	

	 @ExceptionHandler(SubsidyNotFoundException.class)
	    public ResponseEntity<Object> handleSubsidyNotFound(SubsidyNotFoundException ex) {
	        Map<String, Object> body = new HashMap<>();
	        body.put("timestamp", LocalDateTime.now());
	        body.put("status", HttpStatus.NOT_FOUND.value());
	        body.put("error", "Not Found");
	        body.put("message", ex.getMessage());

	        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
	    }
	 
	 
	 @ExceptionHandler(MethodArgumentNotValidException.class)
	 public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
	         MethodArgumentNotValidException ex,
	         HttpServletRequest request) {

	     Map<String, Object> error = new HashMap<>();
	     error.put("timestamp", LocalDateTime.now());
	     error.put("status", HttpStatus.BAD_REQUEST.value());
	     error.put("error", "Bad Request");
	     error.put("message", "Validation failed. Please review the request.");

	     // Collect field-level validation errors
	     List<Map<String, String>> details = new ArrayList<>();

	     ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
	         Map<String, String> fieldMap = new HashMap<>();
	         fieldMap.put("field", fieldError.getField());
	         fieldMap.put("message", fieldError.getDefaultMessage());
	         details.add(fieldMap);
	     });

	     error.put("details", details);
	     error.put("path", request.getRequestURI());

	     return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	 }
	 
	 
	 
	 @ExceptionHandler(ApplicationNotFoundException.class)
	    public ResponseEntity<String> handleNotFound(ApplicationNotFoundException ex) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	    }

	    @ExceptionHandler(IllegalStateException.class)
	    public ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	    }
	    
	   
	 
	    @ExceptionHandler(EntityNotFoundException.class)
	    public ResponseEntity<String> handleEntityNotFound(EntityNotFoundException ex) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	    }

	    
	    @ExceptionHandler(NoSubsidiesFoundException.class)
	    public ResponseEntity<String> handleNoSubsidiesFound(NoSubsidiesFoundException ex) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	    }

	    
	    
	    
	    


}
