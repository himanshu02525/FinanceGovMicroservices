package com.finance.service;

import java.util.List;

import com.finance.dto.FinancialProgramRequest;
import com.finance.dto.FinancialProgramResponse;
import com.finance.enums.ProgramStatus;

public interface FinancialProgramService {
	FinancialProgramResponse saveProgram(FinancialProgramRequest request);
    FinancialProgramResponse updateProgram(Long id, FinancialProgramRequest request);
    String deleteProgram(Long programId);
    FinancialProgramResponse getProgramById(Long programId);
    List<FinancialProgramResponse> getAllPrograms();
    List<FinancialProgramResponse> getProgramsByStatus(ProgramStatus status);
}
