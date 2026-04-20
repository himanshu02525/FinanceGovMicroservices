package com.finance.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.finance.client.CitizenClient;
import com.finance.dto.*;
import com.finance.enums.*;
import com.finance.exceptions.*;
import com.finance.model.TaxRecord;
import com.finance.repository.TaxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor 
public class TaxationServiceImpl implements TaxationService {

    private final TaxRepository taxRepository; // Persistence layer for tax records
    private final CitizenClient citizenClient; // Interface for external entity validation

 // Inside TaxationServiceImpl.java
    @Override
    @Transactional
    public TaxResponseDTO createTaxRecord(TaxRequestDTO request) {
        // This triggers EntityNotFoundException which the GlobalExceptionHandler catches
        if (!citizenClient.validateCitizen(request.getEntityId())) {
            throw new EntityNotFoundException("Entity ID " + request.getEntityId() + " not found.");
        }

        TaxRecord taxRecord = new TaxRecord();
        taxRecord.setEntityId(request.getEntityId());
        taxRecord.setYear(request.getYear()); 
        taxRecord.setAmount(request.getAmount()); // Ensure entity field is also BigDecimal
        taxRecord.setStatus(TaxStatus.PENDING); 
        
        TaxRecord saved = taxRepository.save(taxRecord); 
        return mapToResponseDTO(saved);
    }

    @Override
    public List<TaxResponseDTO> getAllTaxRecords() { 
        // Fetches all tax filings across the system for administrative auditing
        return taxRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList()); 
    }
    
    @Override
    public TaxResponseDTO getTaxRecordByTaxId(Long id) { 
        // Retrieves a single tax filing by its ID or reports a 404 error
        return mapToResponseDTO(taxRepository.findById(id).orElseThrow(() -> new TaxRecordNotFoundException("Not found"))); 
    }
    
    @Override
    public TaxStatsDTO getTaxStatistics() { 
        // Aggregates count and total amount data for the management dashboard
        return new TaxStatsDTO(taxRepository.countTotalTaxPayers(), taxRepository.calculateTotalRevenue()); 
    }
    
    @Override
    @Transactional
    public List<TaxResponseDTO> verifyTaxRecordsByEntity(Long id, TaxStatus status) { 
        // Updates the verification status for all tax records belonging to one entity
        List<TaxRecord> taxRepo = taxRepository.findByEntityId(id); 
        taxRepo.forEach(x -> x.setStatus(status)); 
        return taxRepository.saveAll(taxRepo).stream().map(this::mapToResponseDTO).collect(Collectors.toList()); 
    }

    @Override
    @Transactional
    public TaxResponseDTO verifySingleTaxRecord(Long taxId, TaxStatus status) {
        // Allows an officer to approve or reject a specific tax filing
        TaxRecord record = taxRepository.findById(taxId)
                .orElseThrow(() -> new TaxRecordNotFoundException("Tax record " + taxId + " not found."));
        record.setStatus(status); 
        return mapToResponseDTO(taxRepository.save(record));
    }
    
    private TaxResponseDTO mapToResponseDTO(TaxRecord record) { 
        // Utility method to convert the internal entity to a public-facing DTO
        return TaxResponseDTO.builder().taxId(record.getTaxId()).entityId(record.getEntityId()).year(record.getYear())
                .amount(record.getAmount()).status(record.getStatus()).build(); 
    }
}