package com.finance.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyRequest {

    @NotNull(message = "Entity ID is required")
    private Long entityId;   

    @NotNull(message = "Program ID is required")
    private Long programId;  

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Double amount;   

    
    private LocalDate date;
    
    
    private Long userId;
}

