package com.finance.dto;

import java.time.LocalDate;
import com.finance.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubsidyApplicationResponse {
    private Long applicationId;      
    private Long entityId;           
    private LocalDate submittedDate; 
    private Long programId;          
    private ApplicationStatus status; 
}
