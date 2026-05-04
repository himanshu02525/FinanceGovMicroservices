package com.finance.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyApplicationRequest {
    
	@NotNull(message = "Entity ID is required")
    private Long entityId;   // Citizen/Business ID

    @NotNull(message = "Program ID is required")
    private Long programId;  // Linked program ID

    // Automatically set to today's date if not provided
    private LocalDate submittedDate = LocalDate.now();

    private Long userId;  
}
