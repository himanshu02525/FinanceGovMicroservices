package com.finance.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.client.CitizenClient;
import com.finance.dto.TaxRequestDTO;
import com.finance.dto.TaxResponseDTO;
import com.finance.dto.TaxStatsDTO;
import com.finance.enums.TaxStatus;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.exceptions.TaxRecordNotFoundException;
import com.finance.model.TaxRecord;
import com.finance.repository.TaxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxationServiceImpl implements TaxationService {

    private final TaxRepository taxRepository; // MySQL repository
    private final CitizenClient citizenClient; // Feign client for entity validation

    @Override
    @Transactional
    public TaxResponseDTO createTaxRecord(TaxRequestDTO request) {
        boolean exists;
        try {
            exists = citizenClient.validateCitizen(request.getEntityId()); // Verify entity existence
        } catch (Exception e) {
            throw new EntityNotFoundException("External Registration Service is unreachable."); // Catch Feign errors
        }

        if (!exists) {
            throw new EntityNotFoundException("Entity ID " + request.getEntityId() + " not found."); // Catch logic errors
        }

        TaxRecord taxRecord = TaxRecord.builder()
                .entityId(request.getEntityId())
                .year(request.getYear())
                .amount(request.getAmount())
                .status(TaxStatus.PENDING)
                .build();

        return mapToResponseDTO(taxRepository.save(taxRecord)); // Save and convert
    }

    @Override
    public List<TaxResponseDTO> getAllTaxRecords() {
        return taxRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList()); // Get all entries
    }

    @Override
    public TaxResponseDTO getTaxRecordByTaxId(Long id) {
        TaxRecord record = taxRepository.findById(id)
                .orElseThrow(() -> new TaxRecordNotFoundException("Tax record ID " + id + " not found.")); // Individual lookup
        return mapToResponseDTO(record);
    }

    @Override
    @Transactional
    public List<TaxResponseDTO> verifyTaxRecordsByEntity(Long id, TaxStatus status) {
        // 1. Fetch records
        List<TaxRecord> records = taxRepository.findByEntityId(id);
        
        // 2. CHECK: If list is empty, throw the exception to trigger the 404 handler
        if (records.isEmpty()) {
            throw new EntityNotFoundException("No tax records found for Entity ID: " + id);
        }
        
        // 3. Update status
        records.forEach(record -> record.setStatus(status));
        
        // 4. Save and map to DTO
        return taxRepository.saveAll(records).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaxResponseDTO verifySingleTaxRecord(Long taxId, TaxStatus status) {
        TaxRecord record = taxRepository.findById(taxId)
                .orElseThrow(() -> new TaxRecordNotFoundException("Tax record " + taxId + " not found.")); // Single update
        record.setStatus(status);
        return mapToResponseDTO(taxRepository.save(record));
    }

    @Override
    public TaxStatsDTO getTaxStatistics() {
        return new TaxStatsDTO(taxRepository.countTotalTaxPayers(), taxRepository.calculateTotalRevenue()); // Stats calculation
    }

    private TaxResponseDTO mapToResponseDTO(TaxRecord record) {
        return TaxResponseDTO.builder()
                .taxId(record.getTaxId())
                .entityId(record.getEntityId())
                .year(record.getYear())
                .amount(record.getAmount())
                .status(record.getStatus())
                .build(); // DTO mapping helper
    }
}