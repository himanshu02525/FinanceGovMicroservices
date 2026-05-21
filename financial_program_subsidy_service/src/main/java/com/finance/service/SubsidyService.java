package com.finance.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.finance.dto.SubsidyRequest;
import com.finance.dto.SubsidyResponse;
import com.finance.dto.SubsidyUpdateRequest;

public interface SubsidyService {
    SubsidyResponse saveSubsidy(SubsidyRequest request);

    List<SubsidyResponse> getAllSubsidies();
    List<SubsidyResponse> getSubsidiesByProgram(Long programId);
    List<SubsidyResponse> getSubsidiesByEntity(Long entityId);

    SubsidyResponse getSubsidyById(Long subsidyId);
    BigDecimal getApprovedAmountByProgram(Long programId);
    
    long getApprovedSubsidies(Long programId);
    Map<String, Object> getSubsidySummary();

	
    SubsidyResponse updateSubsidy(SubsidyUpdateRequest requestBody,Long subsidyId);
}
