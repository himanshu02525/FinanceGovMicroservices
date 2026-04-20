package com.finance.service;

import java.util.List;
import java.util.Map;

import com.finance.dto.SubsidyRequest;
import com.finance.dto.SubsidyResponse;

public interface SubsidyService {
//    SubsidyResponse saveSubsidy(SubsidyRequest request);
	SubsidyResponse saveSubsidy(SubsidyRequest request, Long userId, String email);
    List<SubsidyResponse> getAllSubsidies();
    List<SubsidyResponse> getSubsidiesByProgram(Long programId);
    List<SubsidyResponse> getSubsidiesByEntity(Long entityId);
//    SubsidyResponse updateSubsidyStatus(Long subsidyId, String status);
    SubsidyResponse getSubsidyById(Long subsidyId);
//    BigDecimal getApprovedAmountByProgram(Long programId);
    
    long getApprovedSubsidies(Long programId);
    Map<String, Object> getSubsidySummary();
//	SubsidyResponse approveSubsidy(Long subsidyId, Long userId, String email);
	

}
