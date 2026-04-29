package com.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import com.finance.enums.TaxStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxResponseDTO {
    private Long taxId; 
    private Long entityId; 
    private Integer year; 
    private BigDecimal amount; 
    private TaxStatus status; 
}