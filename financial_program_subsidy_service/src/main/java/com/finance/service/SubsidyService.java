package com.finance.service;

import java.math.BigDecimal;
import java.util.List;

import com.finance.dto.SubsidyRequest;
import com.finance.dto.SubsidyResponse;

public interface SubsidyService {
    SubsidyResponse saveSubsidy(SubsidyRequest request);
    List<SubsidyResponse> getAllSubsidies();
    List<SubsidyResponse> getSubsidiesByProgram(Long programId);
    List<SubsidyResponse> getSubsidiesByEntity(Long entityId);
//    SubsidyResponse updateSubsidyStatus(Long subsidyId, String status);
    SubsidyResponse getSubsidyById(Long subsidyId);
    BigDecimal getApprovedAmountByProgram(Long programId);
}
