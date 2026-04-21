package com.finance.service;

import java.util.List;
import com.finance.dto.TaxRequestDTO;
import com.finance.dto.TaxResponseDTO;
import com.finance.dto.TaxStatsDTO;
import com.finance.enums.TaxStatus;

public interface TaxationService {
    // Create record and notify of pending status
    TaxResponseDTO createTaxRecord(TaxRequestDTO request); 
    
    // Admin: List all tax records
    List<TaxResponseDTO> getAllTaxRecords(); 
    
    // Fetch single record by ID
    TaxResponseDTO getTaxRecordByTaxId(Long taxId); 
    
    // Bulk validation for entity tax history
    List<TaxResponseDTO> verifyTaxRecordsByEntity(Long entityId, TaxStatus newStatus); 
    
    // Officer: Validate payment and notify user of success
    TaxResponseDTO verifySingleTaxRecord(Long taxId, TaxStatus newStatus); 
    
    // Dashboard: Aggregate fiscal statistics
    TaxStatsDTO getTaxStatistics();
}