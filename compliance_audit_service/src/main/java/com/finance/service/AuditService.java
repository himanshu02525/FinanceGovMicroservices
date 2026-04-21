package com.finance.service;

import java.util.List;
import java.util.Map;

import com.finance.dto.AuditCreateRequest;
import com.finance.dto.AuditResponse;
import com.finance.dto.AuditUpdateRequest;

import jakarta.validation.Valid;

public interface AuditService {

	List<AuditResponse> findAll();

	Map<String, Integer> getSummary();

	AuditResponse findById(long id);

	AuditResponse create(@Valid AuditCreateRequest body);

	AuditResponse update(long auditId, AuditUpdateRequest auditRecord);

	String delete(long auditId);

	List<AuditResponse> findByOfficerId(long officerId);
}