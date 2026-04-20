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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/compliance")
public class ComplianceController {

	private final ComplianceRecordService service;

	public ComplianceController(ComplianceRecordService service) {
		this.service = service;
	}

	@GetMapping
	public ResponseEntity<List<ComplianceResponse>> getAll() {
		log.info("Request to fetch all compliance records");
		List<ComplianceResponse> records = service.findAll();
		log.info("Fetched {} compliance records", records.size());
		return ResponseEntity.ok(records);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ComplianceResponse> getById(@PathVariable long id) {
		log.info("Request to fetch compliance record with ID: {}", id);
		ComplianceResponse response = service.findById(id);
		log.info("Compliance record retrieved successfully for ID: {}", id);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/entity/{entityId}")
	public ResponseEntity<List<ComplianceResponse>> findByEntityId(@PathVariable long entityId) {
		log.info("Request to fetch compliance records for Entity ID: {}", entityId);
		List<ComplianceResponse> records = service.findByEntityId(entityId);
		log.info("Fetched {} compliance records for Entity ID: {}", records.size(), entityId);
		return ResponseEntity.ok(records);
	}

	@GetMapping("/summary")
	public ResponseEntity<Map<String, Integer>> getSummary() {
		log.info("Request to fetch compliance summary");
		Map<String, Integer> summary = service.getSummary();
		log.info("Compliance summary fetched successfully");
		return ResponseEntity.ok(summary);
	}

	@PostMapping
	public ResponseEntity<ComplianceResponse> create(@Valid @RequestBody ComplianceCreateRequest body) {
		log.info("Request to create new compliance record");
		ComplianceResponse response = service.create(body);
		log.info("Compliance record created successfully with ID: {}", response.getComplianceId());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ComplianceResponse> update(@PathVariable long id,
			@Valid @RequestBody ComplianceUpdateRequest body) {
		log.info("Request to update compliance record with ID: {}", id);
		ComplianceResponse response = service.update(id, body);
		log.info("Compliance record updated successfully with ID: {}", id);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable long id) {
		log.warn("Request to delete compliance record with ID: {}", id);
		service.delete(id);
		log.info("Compliance record deleted successfully with ID: {}", id);
		return ResponseEntity.noContent().build();
	}
}