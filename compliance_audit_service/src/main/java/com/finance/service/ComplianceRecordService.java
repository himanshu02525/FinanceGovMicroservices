package com.finance.service;

import java.util.List;
import java.util.Map;

import com.finance.dto.ComplianceCreateRequest;
import com.finance.dto.ComplianceResponse;
import com.finance.dto.ComplianceUpdateRequest;

public interface ComplianceRecordService {
	List<ComplianceResponse> findAll();

	List<ComplianceResponse> findByEntityId(long entityId);

	Map<String, Integer> getSummary();

	ComplianceResponse findById(long complianceId);

	ComplianceResponse create(ComplianceCreateRequest complianceRecord);

	ComplianceResponse update(long complianceId, ComplianceUpdateRequest complianceRecord);

	String delete(long complianceId);

}