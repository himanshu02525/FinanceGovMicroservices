package com.financegov.service;

import java.util.List;

import com.financegov.dto.BudgetAllocationRequestDTO;
import com.financegov.dto.BudgetAllocationResponseDTO;
import com.financegov.dto.BudgetSummaryDTO;

public interface BudgetAllocationService {
	BudgetAllocationResponseDTO createAllocation(BudgetAllocationRequestDTO dto);

	List<BudgetAllocationResponseDTO> getAllAllocations();

	BudgetSummaryDTO getBudgetSummary(Long programId);

	String deleteAllocation(Long allocationId);
}// Future we deduct Budget After  Subsidy Approval 