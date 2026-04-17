package com.finance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.model.FinancialProgram;
import com.finance.enums.ProgramStatus;

@Repository
public interface FinancialProgramRepository extends JpaRepository<FinancialProgram, Long> {
    List<FinancialProgram> getProgramsByStatus(ProgramStatus status);
}
