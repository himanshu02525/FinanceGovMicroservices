package com.finance.service;

import java.util.List;

import com.finance.dto.BudgetAllocationRequestDTO;
import com.finance.dto.BudgetAllocationResponseDTO;
import com.finance.dto.BudgetSummaryDTO;

public interface BudgetAllocationService {

    BudgetAllocationResponseDTO createAllocation(BudgetAllocationRequestDTO dto);

    List<BudgetAllocationResponseDTO> getAllAllocations();

    BudgetSummaryDTO getBudgetSummary(Long programId);

    String deleteAllocation(Long allocationId);

    // Future: We deduct budget after subsidy approval
}