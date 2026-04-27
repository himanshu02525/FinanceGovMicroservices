package com.finance.client;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.finance.dto.FinancialProgramResponseDTO;

@Component
public class FinancialProgramClientFallback implements FinancialProgramClient {

    @Override
    public FinancialProgramResponseDTO getProgramById(Long programId) {
        // return safe default or empty object
        FinancialProgramResponseDTO dto = new FinancialProgramResponseDTO();
        dto.setProgramId(programId);
        dto.setBudget(0.0);
        dto.setStatus("FINANCIAL PROGRAM SERVICE UNAVAILABLE [CURRENTLY DOWN]");
        return dto;
    }

    @Override
    public BigDecimal getApprovedAmount(Long programId) {
        // fallback value
        return BigDecimal.ZERO;
    }
}
