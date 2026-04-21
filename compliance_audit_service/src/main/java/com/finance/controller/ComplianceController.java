package com.finance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.ComplianceCreateRequest;
import com.finance.dto.ComplianceResponse;
import com.finance.dto.ComplianceUpdateRequest;
import com.finance.service.ComplianceRecordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/compliance")
@RequiredArgsConstructor
public class ComplianceController {

	private final ComplianceRecordService service;

	@GetMapping
	public ResponseEntity<List<ComplianceResponse>> getAll() {
		return ResponseEntity.ok(service.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ComplianceResponse> getById(@PathVariable long id) {
		return ResponseEntity.ok(service.findById(id));
	}

	@GetMapping("/entity/{entityId}")
	public ResponseEntity<List<ComplianceResponse>> findByEntityId(@PathVariable long entityId) {
		return ResponseEntity.ok(service.findByEntityId(entityId));
	}

	@GetMapping("/summary")
	public ResponseEntity<Map<String, Integer>> getSummary() {
		return ResponseEntity.ok(service.getSummary());
	}

	@PostMapping
	public ResponseEntity<ComplianceResponse> create(@Valid @RequestBody ComplianceCreateRequest body) {
		return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ComplianceResponse> update(@PathVariable long id,
			@Valid @RequestBody ComplianceUpdateRequest body) {
		return ResponseEntity.ok(service.update(id, body));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable long id) {
		return ResponseEntity.ok(service.delete(id));
	}
}
