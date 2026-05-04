package com.finance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.AuditCreateRequest;
import com.finance.dto.AuditResponse;
import com.finance.dto.AuditUpdateRequest;
import com.finance.service.AuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/audit")

@CrossOrigin("*")
public class AuditController {

	private final AuditService service;

	@GetMapping
	public ResponseEntity<List<AuditResponse>> getAll() {
		return ResponseEntity.ok(service.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<AuditResponse> getById(@PathVariable long id) {
		return ResponseEntity.ok(service.findById(id));
	}

	@GetMapping("/officer/{id}")
	public ResponseEntity<List<AuditResponse>> findByOfficerId(@PathVariable long id) {
		return ResponseEntity.ok(service.findByOfficerId(id));
	}

	@PostMapping
	public ResponseEntity<AuditResponse> create(@Valid @RequestBody AuditCreateRequest body) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<AuditResponse> update(@PathVariable long id, @Valid @RequestBody AuditUpdateRequest body) {
		return ResponseEntity.ok(service.update(id, body));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable long id) {
		return ResponseEntity.ok(service.delete(id));
	}

	@GetMapping("/summary")
	public ResponseEntity<Map<String, Integer>> getSummary() {
		return ResponseEntity.ok(service.getSummary());
	}
}
