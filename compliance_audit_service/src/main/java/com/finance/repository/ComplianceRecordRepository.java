package com.finance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finance.enums.ComplianceRecordResult;
import com.finance.model.ComplianceRecord;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Long> {

	List<ComplianceRecord> findByEntityId(long entityId);

	@Enumerated(EnumType.STRING)
	int countByResult(ComplianceRecordResult value);

}