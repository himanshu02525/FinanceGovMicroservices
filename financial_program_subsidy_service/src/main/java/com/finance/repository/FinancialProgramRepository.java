package com.finance.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.finance.enums.ProgramStatus;
import com.finance.model.FinancialProgram;

@Repository
public interface FinancialProgramRepository extends JpaRepository<FinancialProgram, Long> {
	List<FinancialProgram> getProgramsByStatus(ProgramStatus status);

	@Override
	long count(); 

	long countByStatus(ProgramStatus status);

	@Query("SELECT COALESCE(SUM(p.budget), 0) FROM FinancialProgram p")
	BigDecimal sumTotalBudgetAcrossAllPrograms(); 

}
