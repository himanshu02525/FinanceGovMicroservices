package com.finance.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyRequest {

    @NotNull(message = "Entity ID is required")
    private Long entityId;   // Citizen/Business applying

    @NotNull(message = "Program ID is required")
    private Long programId;  // Financial program ID

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;   // Requested subsidy amount

    // Optional: if not provided, defaults to LocalDate.now() in service
    private LocalDate date;
    
    private String status;
    
    private Long userId;
}

