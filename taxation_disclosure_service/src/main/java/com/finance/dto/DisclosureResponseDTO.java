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
    private Long disclosureId; // Unique ID for the disclosure
    private Long entityId; // The ID of the citizen/business who filed it
    private DisclosureType type; // The type (INCOME/EXPENSE)
    private DisclosureStatus status; // The status (SUBMITTED, VALIDATED, REJECTED)
    private LocalDateTime submissionDate;
}