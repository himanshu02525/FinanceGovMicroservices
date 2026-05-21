


package com.finance.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubsidyResponse {
    private Long subsidyId;     
    private Long entityId;      
    private Double amount;      
    private LocalDate date;     
    private String status;      
    private Long programId;     
}

