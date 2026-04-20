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

import com.finance.dto.AuditCreateRequest;
import com.finance.dto.AuditResponse;
import com.finance.dto.AuditUpdateRequest;
import com.finance.service.AuditService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/audit")
@Slf4j
public class AuditController {

	private final AuditService service;

	public AuditController(AuditService service) {
		this.service = service;
	}

	@GetMapping
	public ResponseEntity<List<AuditResponse>> getAll() {
		log.info("Request to fetch all audit records");
		List<AuditResponse> audits = service.findAll();
		log.info("Fetched {} audit records", audits.size());
		return ResponseEntity.ok(audits);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AuditResponse> getById(@PathVariable long id) {
		log.info("Request to fetch audit record with ID: {}", id);
		AuditResponse response = service.findById(id);
		log.info("Audit record retrieved successfully for ID: {}", id);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/officer/{id}")
	public ResponseEntity<List<AuditResponse>> findByOfficerId(@PathVariable long id) {
		log.info("Request to fetch audit records for Officer ID: {}", id);
		List<AuditResponse> audits = service.findByOfficerId(id);
		log.info("Fetched {} audit records for Officer ID: {}", audits.size(), id);
		return ResponseEntity.ok(audits);
	}

	@PostMapping
	public ResponseEntity<AuditResponse> create(@Valid @RequestBody AuditCreateRequest body) {
		log.info("Request to create new audit record");
		AuditResponse response = service.create(body);
		log.info("Audit record created successfully with ID: {}", response.getAuditId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<AuditResponse> update(@PathVariable long id, @Valid @RequestBody AuditUpdateRequest body) {
		log.info("Request to update audit record with ID: {}", id);
		AuditResponse response = service.update(id, body);
		log.info("Audit record updated successfully with ID: {}", id);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		log.warn("Request to delete audit record with ID: {}", id);
		service.delete(id);
		log.info("Audit record deleted successfully with ID: {}", id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/summary")
	public ResponseEntity<Map<String, Integer>> getSummary() {
		log.info("Request to fetch audit summary");
		Map<String, Integer> summary = service.getSummary();
		log.info("Audit summary fetched successfully");
		return ResponseEntity.ok(summary);
	}
}