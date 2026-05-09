package com.finance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.finance.enums.ApplicationStatus;
import com.finance.model.SubsidyApplication;

@Repository
public interface SubsidyApplicationRepository extends JpaRepository<SubsidyApplication, Long> {

	List<SubsidyApplication> findByStatus(String status);

	@Query("SELECT s FROM SubsidyApplication s WHERE s.program.id = :programId")
	List<SubsidyApplication> findByProgramId(Long programId);

	long countByProgramAndStatus(Long program, ApplicationStatus status);

	List<SubsidyApplication> findByEntityId(Long entityId);

	long countByProgramProgramId(Long programId);

	long countByStatus(ApplicationStatus status);
}
