package com.finance.service;

import java.util.List;

import com.finance.dto.SubsidyApplicationRequest;
import com.finance.dto.SubsidyApplicationResponse;

public interface SubsidyApplicationService {
    SubsidyApplicationResponse saveApplication(SubsidyApplicationRequest request);
    List<SubsidyApplicationResponse> getApplicationsByEntity(Long entityId);
    SubsidyApplicationResponse approveApplication(Long applicationId);
    SubsidyApplicationResponse rejectApplication(Long applicationId);
    long getApplicationsReceived(Long programId);
	
}


