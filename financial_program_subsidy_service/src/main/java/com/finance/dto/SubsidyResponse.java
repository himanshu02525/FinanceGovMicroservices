


package com.finance.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubsidyResponse {
    private Long subsidyId;     // Auto-generated ID
    private Long entityId;      // Citizen/Business ID
    private Double amount;      // Subsidy amount
    private LocalDate date;     // Date of subsidy
    private String status;      
    private Long programId;     // Linked program ID
}

