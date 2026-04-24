package com.finance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.TaxRequestDTO;
import com.finance.dto.TaxResponseDTO;
import com.finance.enums.TaxStatus;
import com.finance.service.TaxationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/taxation")
@RequiredArgsConstructor
public class TaxationController {

    private final TaxationService taxationService;

    @PostMapping("/enter_taxrecord")
    public ResponseEntity<TaxResponseDTO> createTaxRecord(@Valid @RequestBody TaxRequestDTO request) {
        // Triggers creation logic and catches validation errors via GlobalExceptionHandler
        return ResponseEntity.ok(taxationService.createTaxRecord(request));
    }

    @GetMapping("/taxrecords/{taxId}")
    public ResponseEntity<TaxResponseDTO> getTaxRecordByTaxId(@PathVariable Long taxId) {
        // Returns 200 OK or 404 via custom exception
        return ResponseEntity.ok(taxationService.getTaxRecordByTaxId(taxId));
    }

    @GetMapping("/all_taxrecords")
    public ResponseEntity<List<TaxResponseDTO>> getAllRecords() {
        // Returns list of all tax entries for government audit
        return ResponseEntity.ok(taxationService.getAllTaxRecords());
    }

    @GetMapping("/tax/summary")
    public ResponseEntity<Map<String, Object>> getTaxStatistics() {
        // Provides aggregated fiscal data for management oversight
        return ResponseEntity.ok(taxationService.getTaxStatistics());
    }

    @GetMapping("/taxrecords/entity/{entityId}")
    public ResponseEntity<List<TaxResponseDTO>> getTaxRecordsByEntityId(@PathVariable("entityId") Long entityId){
    	return ResponseEntity.ok(taxationService.getAllTaxRecordsByEntityId(entityId));
    }

    @PatchMapping("/taxrecords/verify/{taxId}")
    public ResponseEntity<TaxResponseDTO> verifySingleTax(@PathVariable Long taxId, @RequestParam TaxStatus status) {
        // Approves or rejects a single record by a financial officer
        return ResponseEntity.ok(taxationService.verifySingleTaxRecord(taxId, status));
    }
}