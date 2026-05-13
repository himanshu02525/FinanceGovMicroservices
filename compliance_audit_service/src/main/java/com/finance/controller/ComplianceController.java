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
import lombok.extern.slf4j.Slf4j; // 1. Import Slf4j

@RestController
@RequestMapping("/compliance")
@RequiredArgsConstructor
@Slf4j
public class ComplianceController {

	private final ComplianceRecordService service;

	@GetMapping
	public ResponseEntity<List<ComplianceResponse>> getAll() {
		log.info("REST request to get all Compliance records");
		List<ComplianceResponse> response = service.findAll();
		log.debug("Found {} compliance records", response.size());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ComplianceResponse> getById(@PathVariable long id) {
		log.info("REST request to get Compliance record : {}", id);
		return ResponseEntity.ok(service.findById(id));
	}

	@GetMapping("/entity/{entityId}")
	public ResponseEntity<List<ComplianceResponse>> findByEntityId(@PathVariable long entityId) {
		log.info("REST request to get Compliance records for Entity ID : {}", entityId);
		return ResponseEntity.ok(service.findByEntityId(entityId));
	}

	@GetMapping("/summary")
	public ResponseEntity<Map<String, Integer>> getSummary() {
		log.info("REST request to get Compliance summary");
		return ResponseEntity.ok(service.getSummary());
	}

	@PostMapping
	public ResponseEntity<ComplianceResponse> create(@Valid @RequestBody ComplianceCreateRequest body) {
		log.info("REST request to save Compliance record : {}", body);
		ComplianceResponse result = service.create(body);
		log.info("Successfully created Compliance record with ID: {}", result.getComplianceId());
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ComplianceResponse> update(@PathVariable long id,
			@Valid @RequestBody ComplianceUpdateRequest body) {
		log.info("REST request to update Compliance record ID: {} with data: {}", id, body);
		return ResponseEntity.ok(service.update(id, body));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable long id) {
		log.info("REST request to delete Compliance record : {}", id);
		String response = service.delete(id);
		log.info("Compliance record {} deleted successfully", id);
		return ResponseEntity.ok(response);
	}
}