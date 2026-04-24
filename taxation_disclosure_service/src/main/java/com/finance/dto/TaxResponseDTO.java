package com.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.finance.enums.TaxStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxResponseDTO {
    private Long taxId; // Unique database ID for this record
    private Long entityId; // The ID of the owner (Citizen/Business)
    private Integer year; // The fiscal year of the record
    private BigDecimal amount; // The reported tax amount
    private TaxStatus status; // The current workflow status (e.g., PENDING, PAID)
}