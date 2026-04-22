package com.finance.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.finance.dto.*;
import com.finance.enums.TaxStatus;
import com.finance.service.TaxationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/taxation")
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
    
    @GetMapping("/taxrecords/entity/{entityId}")
    public ResponseEntity<List<TaxResponseDTO>> getTaxRecordsByEntityId(@PathVariable("entityId") Long entityId){
    	return ResponseEntity.ok(taxationService.getAllTaxRecordsByEntityId(entityId));
    }

    @GetMapping("/tax/summary")
    public ResponseEntity<TaxStatsDTO> getTaxStatistics() {
        // Provides aggregated fiscal data for management oversight
        return ResponseEntity.ok(taxationService.getTaxStatistics());
    }


    @PatchMapping("/taxrecords/{taxId}/verify")
    public ResponseEntity<TaxResponseDTO> verifySingleTax(@PathVariable Long taxId, @RequestParam TaxStatus status) {
        // Approves or rejects a single record by a financial officer
        return ResponseEntity.ok(taxationService.verifyTaxRecordByTaxId(taxId, status));
    }
}