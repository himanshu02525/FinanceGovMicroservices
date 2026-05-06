


package com.finance.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyResponse {
    private Long subsidyId;     // Auto-generated ID
    private Long entityId;      // Citizen/Business ID
    private Double amount;      // Subsidy amount
    private LocalDate date;     // Date of subsidy
    private String status;      
    private Long programId;     // Linked program ID
}

