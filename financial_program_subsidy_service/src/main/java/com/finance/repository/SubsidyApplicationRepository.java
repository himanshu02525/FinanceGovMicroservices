package com.finance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.model.SubsidyApplication;

@Repository
public interface SubsidyApplicationRepository extends JpaRepository<SubsidyApplication, Long> {
	List<SubsidyApplication> findByProgramProgramId(Long programId);

	List<SubsidyApplication> findByStatus(String status);

	
	
	List<SubsidyApplication> findByEntityId(Long entityId);
	
	long countByProgramProgramId(Long programId);

}
