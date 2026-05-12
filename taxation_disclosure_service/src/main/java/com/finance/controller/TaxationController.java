package com.finance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.TaxRequestDTO;
import com.finance.dto.TaxResponseDTO;
import com.finance.dto.TaxUpdateDTO;
import com.finance.service.TaxationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController

@RequestMapping("/api/taxation")

@RequiredArgsConstructor

public class TaxationController {

	private final TaxationService taxationService;

	@PostMapping("/enter_taxrecord")
	public ResponseEntity<TaxResponseDTO> createTaxRecord(@Valid @RequestBody TaxRequestDTO request) {
		log.info("REST request to create TaxRecord for Entity ID: {}", request.getEntityId());
		TaxResponseDTO response = taxationService.createTaxRecord(request);
		log.info("Successfully created TaxRecord with ID: {}", response.getTaxId());
		return ResponseEntity.ok(response);
	}

	@GetMapping("/taxrecords/{taxId}")
	public ResponseEntity<TaxResponseDTO> getTaxRecordByTaxId(@PathVariable Long taxId) {
		log.info("REST request to get TaxRecord by ID: {}", taxId);
		return ResponseEntity.ok(taxationService.getTaxRecordByTaxId(taxId));
	}

	@GetMapping("/all_taxrecords")
	public ResponseEntity<List<TaxResponseDTO>> getAllRecords() {
		log.info("REST request to get all tax records for audit");
		List<TaxResponseDTO> records = taxationService.getAllTaxRecords();
		log.debug("Returning {} tax records", records.size());
		return ResponseEntity.ok(records);
	}

	@GetMapping("/tax/summary")
	public ResponseEntity<Map<String, Object>> getTaxStatistics(@RequestParam(required = false) Integer year) {
		log.info("REST request to get aggregated tax statistics");
		return ResponseEntity.ok(taxationService.getTaxStatistics(year));
	}

	@GetMapping("/taxrecords/entity/{entityId}")
	public ResponseEntity<List<TaxResponseDTO>> getTaxRecordsByEntityId(@PathVariable("entityId") Long entityId) {
		log.info("REST request to get all tax records for Entity ID: {}", entityId);
		return ResponseEntity.ok(taxationService.getAllTaxRecordsByEntityId(entityId));
	}

	@PutMapping("/taxrecords/verify/{taxId}")
	public ResponseEntity<TaxResponseDTO> verifySingleTax(@PathVariable Long taxId,
			@RequestBody TaxUpdateDTO taxUpdateDTO) {
		log.info("REST request to verify TaxRecord ID: {} with status: {}", taxId, taxUpdateDTO.getStatus());
		TaxResponseDTO response = taxationService.verifyTaxRecordByTaxId(taxId, taxUpdateDTO);
		log.info("TaxRecord ID: {} verification completed. New status: {}", taxId, response.getStatus());
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/taxrecords/pay/{taxId}")
	public ResponseEntity<TaxResponseDTO> payTaxRecord(@PathVariable Long taxId) {
		log.info("REST request to pay TaxRecord ID: {}", taxId);
		TaxResponseDTO response = taxationService.payTaxRecordByTaxId(taxId);
		log.info("TaxRecord ID: {} payment completed. New status: {}", taxId, response.getStatus());
		return ResponseEntity.ok(response);
	}

}
