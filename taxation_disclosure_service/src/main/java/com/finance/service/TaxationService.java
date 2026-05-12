package com.finance.service;

import java.util.List;
import java.util.Map;

import com.finance.dto.TaxRequestDTO;
import com.finance.dto.TaxResponseDTO;
import com.finance.dto.TaxUpdateDTO;

public interface TaxationService {

    // Create record and notify of pending status
    TaxResponseDTO createTaxRecord(TaxRequestDTO request); 
    

    
    TaxResponseDTO payTaxRecordByTaxId(Long taxId);
    



	// Admin: List all tax records
	List<TaxResponseDTO> getAllTaxRecords();

	// Fetch single record by ID
	TaxResponseDTO getTaxRecordByTaxId(Long taxId);

	List<TaxResponseDTO> getAllTaxRecordsByEntityId(Long entityId);

	// Officer: Validate payment and notify user of success
	TaxResponseDTO verifyTaxRecordByTaxId(Long taxId, TaxUpdateDTO taxUpdateDTO);

	// Dashboard: Aggregate fiscal statistics
	Map<String, Object> getTaxStatistics(Integer year);
}