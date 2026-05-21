


package com.finance.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import lombok.AllArgsConstructor;

@Data                     
@NoArgsConstructor        
@AllArgsConstructor       
public class FinancialProgramResponse {

    private Long programId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double budget;
    private String status;
}

