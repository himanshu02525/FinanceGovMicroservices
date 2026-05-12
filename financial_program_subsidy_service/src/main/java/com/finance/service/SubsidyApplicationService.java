package com.finance.service;

import java.util.List;

import com.finance.dto.SubsidyApplicationRequest;
import com.finance.dto.SubsidyApplicationResponse;
import com.finance.model.SubsidyApplication;

public interface SubsidyApplicationService {
    SubsidyApplicationResponse saveApplication(SubsidyApplicationRequest request);
    List<SubsidyApplicationResponse> getApplicationsByEntity(Long entityId);
	List<SubsidyApplicationResponse> fetchByProgram(Long programId);
    SubsidyApplicationResponse approveApplication(Long applicationId);
    SubsidyApplicationResponse rejectApplication(Long applicationId);
    long getApplicationsReceived(Long programId);
}


