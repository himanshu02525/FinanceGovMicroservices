package com.finance.service;

import java.util.List;
import java.util.Map;

import com.finance.dto.TaxRequestDTO;
import com.finance.dto.TaxResponseDTO;
import com.finance.enums.TaxStatus;

public interface TaxationService {
    // Create record and notify of pending status
    TaxResponseDTO createTaxRecord(TaxRequestDTO request); 
    
    // Admin: List all tax records
    List<TaxResponseDTO> getAllTaxRecords(); 
    
    // Fetch single record by ID
    TaxResponseDTO getTaxRecordByTaxId(Long taxId); 
    
    List<TaxResponseDTO> getAllTaxRecordsByEntityId(Long entityId);
    
    // Officer: Validate payment and notify user of success
    TaxResponseDTO verifySingleTaxRecord(Long taxId, TaxStatus newStatus); 
    
    // Dashboard: Aggregate fiscal statistics
    Map<String, Object> getTaxStatistics();
}