package com.finance.service;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.client.CitizenClient;
import com.finance.client.NotificationFeignClient;
import com.finance.client.UserFeignClient;
import com.finance.dto.NotificationRequestDto;
import com.finance.dto.TaxRequestDTO;
import com.finance.dto.TaxResponseDTO;
import com.finance.dto.TaxStatsDTO;
import com.finance.dto.UserDto;
import com.finance.enums.NotificationCategory;
import com.finance.enums.TaxStatus;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.exceptions.InvalidTaxYearException;
import com.finance.exceptions.TaxRecordNotFoundException;
import com.finance.model.TaxRecord;
import com.finance.repository.TaxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxationServiceImpl implements TaxationService {

    private final TaxRepository taxRepository;
    private final CitizenClient citizenClient;
    private final UserFeignClient userFeignClient;
    private final NotificationFeignClient notificationFeignClient;

    @Override
    @Transactional
    public TaxResponseDTO createTaxRecord(TaxRequestDTO request) {
        // 1. Verify Entity Existence
        Boolean exists;
        try {
            exists = citizenClient.validateCitizen(request.getEntityId());
        } catch (Exception e) {
            throw new EntityNotFoundException("Registration Service is unreachable.");
        }

        if (exists == null || !exists) {
            throw new EntityNotFoundException("Entity ID " + request.getEntityId() + " not found.");
        }
        
        int currentYear = Year.now().getValue();
        int requestYear = request.getYear();

        // Check if the year is neither the current year nor the past year
        if (requestYear != currentYear && requestYear != (currentYear - 1)) {
            throw new InvalidTaxYearException("Invalid Tax Year: " + requestYear + 
                ". You can only file for the current year (" + currentYear + 
                ") or the previous year (" + (currentYear - 1) + ").");
        }
        // 2. Map and Save
        TaxRecord taxRecord = new TaxRecord();
        taxRecord.setEntityId(request.getEntityId());
        taxRecord.setYear(request.getYear());
        taxRecord.setAmount(request.getAmount());
        taxRecord.setStatus(TaxStatus.PENDING);

        TaxRecord saved = taxRepository.save(taxRecord);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public List<TaxResponseDTO> verifyTaxRecordsByEntity(Long id, TaxStatus status) {
        List<TaxRecord> records = taxRepository.findByEntityId(id);
        
        if (records.isEmpty()) {
            throw new EntityNotFoundException("No tax records found for Entity ID: " + id);
        }
        
        records.forEach(record -> record.setStatus(status));
        List<TaxRecord> savedRecords = taxRepository.saveAll(records);

     
        if (status == TaxStatus.PAID || status == TaxStatus.OVERDUE) {
            try {
                UserDto user = userFeignClient.getUserById(id);
                if (user != null && user.getEmail() != null) {
                    String message = (status == TaxStatus.PAID) ? "Payment Successful" : "Your payment is overdue, pay it";
                    
                    NotificationRequestDto notification = NotificationRequestDto.builder()
                            .userId(user.getUserId())
                            .entityId(id)
                            .category(NotificationCategory.TAX)
                            .message(message)
                            .build();

                    notificationFeignClient.sendNotification(notification, user.getEmail());
                }
            } catch (Exception e) {
                log.error("Bulk tax notification failed for entity {}: {}", id, e.getMessage());
            }
        }

        return savedRecords.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaxResponseDTO verifySingleTaxRecord(Long taxId, TaxStatus status) {
        TaxRecord record = taxRepository.findById(taxId)
                .orElseThrow(() -> new TaxRecordNotFoundException("Tax record " + taxId + " not found."));
        
        record.setStatus(status);
        TaxRecord saved = taxRepository.save(record);

        
        if (status == TaxStatus.PAID || status == TaxStatus.OVERDUE) {
            try {
                UserDto user = userFeignClient.getUserById(saved.getEntityId());
                if (user != null && user.getEmail() != null) {
                    String message = (status == TaxStatus.PAID) ? "Payment Successful" : "Your payment is overdue, pay it";
                    
                    NotificationRequestDto notification = NotificationRequestDto.builder()
                            .userId(user.getUserId())
                            .entityId(saved.getEntityId())
                            .category(NotificationCategory.TAX)
                            .message(message)
                            .build();

                    notificationFeignClient.sendNotification(notification, user.getEmail());
                }
            } catch (Exception e) {
                log.error("Single tax notification failed for tax record {}: {}", taxId, e.getMessage());
            }
        }

        return mapToResponseDTO(saved);
    }

    @Override
    public List<TaxResponseDTO> getAllTaxRecords() {
        return taxRepository.findAll().stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public TaxResponseDTO getTaxRecordByTaxId(Long id) {
        TaxRecord record = taxRepository.findById(id)
                .orElseThrow(() -> new TaxRecordNotFoundException("Tax record ID " + id + " not found."));
        return mapToResponseDTO(record);
    }

    @Override
    public TaxStatsDTO getTaxStatistics() {
        return new TaxStatsDTO(taxRepository.countTotalTaxPayers(), taxRepository.calculateTotalRevenue());
    }

    private TaxResponseDTO mapToResponseDTO(TaxRecord record) {
        TaxResponseDTO dto = new TaxResponseDTO();
        dto.setTaxId(record.getTaxId());
        dto.setEntityId(record.getEntityId());
        dto.setYear(record.getYear());
        dto.setAmount(record.getAmount());
        dto.setStatus(record.getStatus());
        return dto;
    }
}