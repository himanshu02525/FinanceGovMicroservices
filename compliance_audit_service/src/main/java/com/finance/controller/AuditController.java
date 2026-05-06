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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Step 1: Import Slf4j

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/audit")
public class AuditController {

	private final AuditService service;

	@GetMapping
	public ResponseEntity<List<AuditResponse>> getAll() {
		log.info("REST request to get all Audits");
		List<AuditResponse> list = service.findAll();
		log.debug("Found {} audit records", list.size());
		return ResponseEntity.ok(list);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AuditResponse> getById(@PathVariable long id) {
		log.info("REST request to get Audit by ID: {}", id);
		return ResponseEntity.ok(service.findById(id));
	}

	@GetMapping("/officer/{id}")
	public ResponseEntity<List<AuditResponse>> findByOfficerId(@PathVariable long id) {
		log.info("REST request to get Audits for Officer ID: {}", id);
		return ResponseEntity.ok(service.findByOfficerId(id));
	}

	@PostMapping
	public ResponseEntity<AuditResponse> create(@Valid @RequestBody AuditCreateRequest body) {
		log.info("REST request to create Audit: {}", body);
		AuditResponse response = service.create(body);
		log.info("Successfully created Audit with ID: {}", response.getAuditId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<AuditResponse> update(@PathVariable long id, @Valid @RequestBody AuditUpdateRequest body) {
		log.info("REST request to update Audit ID: {} with data: {}", id, body);
		return ResponseEntity.ok(service.update(id, body));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable long id) {
		log.info("REST request to delete Audit ID: {}", id);
		String result = service.delete(id);
		log.info("Audit deletion result for ID {}: {}", id, result);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/summary")
	public ResponseEntity<Map<String, Integer>> getSummary() {
		log.info("REST request to get Audit status summary");
		return ResponseEntity.ok(service.getSummary());
	}
}