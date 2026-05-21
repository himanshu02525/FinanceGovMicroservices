package com.finance.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyApplicationRequest {
    
	@NotNull(message = "Entity ID is required")
    private Long entityId;   

    @NotNull(message = "Program ID is required")
    private Long programId;  

    
    private LocalDate submittedDate = LocalDate.now();

    private Long userId;  
}
