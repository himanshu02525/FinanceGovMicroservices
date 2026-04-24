package com.finance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.enums.AuditStatus;
import com.finance.model.Audit;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {

	List<Audit> findByOfficerId(long entityId);

	@Enumerated(EnumType.STRING)
	int countByStatus(AuditStatus financial);

}