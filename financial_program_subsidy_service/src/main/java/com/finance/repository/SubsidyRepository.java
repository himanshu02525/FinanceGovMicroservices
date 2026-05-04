package com.finance.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.finance.enums.SubsidyStatus;
import com.finance.model.Subsidy;

import feign.Param;

@Repository
public interface SubsidyRepository extends JpaRepository<Subsidy, Long> {
	List<Subsidy> findByProgramProgramId(Long programId);

	List<Subsidy> findByEntityId(Long entityId);

	@Query("""
			SELECT COALESCE(SUM(s.amount), 0)
			FROM Subsidy s
			WHERE s.program.programId = :programId
			AND s.status = 'GRANTED'
			""")
	BigDecimal sumApprovedAmountByProgramId(@Param("programId") Long programId);

    
	long countByProgramProgramIdAndStatus(Long programId, SubsidyStatus status);

	
	long countByStatus(SubsidyStatus status);

	@Query("SELECT COALESCE(SUM(s.amount), 0) FROM Subsidy s WHERE s.status = 'APPROVED'")
	BigDecimal sumApprovedAmountAcrossAllPrograms();

}