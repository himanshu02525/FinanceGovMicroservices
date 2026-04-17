package com.finance.client.mock;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.finance.client.FinancialProgramClient;
import com.finance.dto.FinancialProgramResponseDTO;
import com.finance.exceptions.ProgramNotFound;

@Component
@Profile("dev") // ✅ active only in dev profile
public class MockFinancialProgramClient implements FinancialProgramClient {

    @Override
    public FinancialProgramResponseDTO getProgramById(Long programId) {

        // ✅ Temporary mock logic
        if (programId == null || programId <= 0) {
            throw new ProgramNotFound(
                "Program not found with ID: " + programId);
        }

        // ✅ Fake ACTIVE program
        return new FinancialProgramResponseDTO(
                programId,
                500000.0,   // mock budget
                "ACTIVE"    // mock status
        );
    }
}