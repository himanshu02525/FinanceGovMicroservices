package com.finance.dto;

import java.time.LocalDate;
import com.finance.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubsidyApplicationResponse {
    private Long applicationId;      // Auto-generated ID
    private Long entityId;           // Citizen/Business ID
    private LocalDate submittedDate; // Date application was created
    private Long programId;          // Linked program ID
    private ApplicationStatus status; // GRANTED / CANCELLED
}
