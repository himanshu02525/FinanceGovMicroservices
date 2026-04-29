package com.finance.dto;

import java.time.LocalDateTime;

import com.finance.enums.DisclosureStatus;
import com.finance.enums.DisclosureType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisclosureResponseDTO {
	// Unique ID for the disclosure
    private Long disclosureId;
    // The ID of the citizen/business who filed it
    private Long entityId; 
    // The type (INCOME/EXPENSE)
    private DisclosureType type; 
    // The status (SUBMITTED, VALIDATED, REJECTED)
    private DisclosureStatus status; 
    private LocalDateTime submissionDate;
}