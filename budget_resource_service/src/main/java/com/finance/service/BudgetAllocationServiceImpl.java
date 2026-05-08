package com.finance.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.finance.client.FinancialProgramClient;
import com.finance.dto.BudgetAllocationRequestDTO;
import com.finance.dto.BudgetAllocationResponseDTO;
import com.finance.dto.BudgetSummaryDTO;
import com.finance.dto.FinancialProgramResponseDTO;
import com.finance.enums.AllocationStatus;
import com.finance.exceptions.AllocationNotFoundException;
import com.finance.exceptions.InvalidAllocationStatusException;
import com.finance.exceptions.ProgramNotFound;
import com.finance.model.BudgetAllocation;
import com.finance.repository.BudgetAllocationRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetAllocationServiceImpl implements BudgetAllocationService {

	private static final Logger logger = LoggerFactory.getLogger(BudgetAllocationServiceImpl.class);

	private final BudgetAllocationRepository budgetAllocationRepository;

	// ✅ NEW – Feign Clients instead of repositories
	private final FinancialProgramClient financialProgramClient;

	private AllocationStatus mapStatus(String status) {
		try {
			return AllocationStatus.valueOf(status.toUpperCase());
		} catch (Exception ex) {
			logger.error("Invalid allocation status received: {}", status);
			throw new InvalidAllocationStatusException("Status must be either ALLOCATED or CANCELLED");
		}
	}

	@Override
	public BudgetAllocationResponseDTO createAllocation(BudgetAllocationRequestDTO dto) {

		logger.info("Creating budget allocation for Program ID: {}", dto.getProgramId());

		// ✅ MICROservice-safe: Validate Program via Feign
		FinancialProgramResponseDTO program;

		try {
			program = financialProgramClient.getProgramById(dto.getProgramId());
		} catch (FeignException ex) {
			// ✅ ALWAYS throw domain exception
			throw new ProgramNotFound("Program not found with ID: " + dto.getProgramId());
		}

		if (!"ACTIVE".equalsIgnoreCase(program.getStatus())) {
			logger.warn("Budget allocation attempt failed. Program ID {} is not ACTIVE. Current status: {}",
					program.getProgramId(), program.getStatus());

			throw new IllegalStateException("Budget can be allocated only when the program status is ACTIVE");
		}

		BudgetAllocation allocation = BudgetAllocation.builder().programId(dto.getProgramId()).amount(dto.getAmount())
				.date(dto.getDate()).status(mapStatus(dto.getStatus())).build();

		BudgetAllocation saved = budgetAllocationRepository.save(allocation);

		logger.info("Budget allocation created successfully. Allocation ID: {}, Amount: {}", saved.getAllocationId(),
				saved.getAmount());

		return BudgetAllocationResponseDTO.builder().allocationId(saved.getAllocationId())
				.programId(saved.getProgramId()).amount(saved.getAmount()).date(saved.getDate())
				.status(saved.getStatus().name()).build();
	}

	@Override
	public List<BudgetAllocationResponseDTO> getAllAllocations() {

		logger.info("Fetching all budget allocations");

		List<BudgetAllocationResponseDTO> responseList = new ArrayList<>();

		List<BudgetAllocation> allocations = budgetAllocationRepository.findAll();
		
		if(allocations.isEmpty())
		{
			throw new AllocationNotFoundException(" No Allocations Present till now ");
		}

		logger.debug("Total allocations found: {}", allocations.size());

		for (BudgetAllocation allocation : allocations) {
			responseList.add(BudgetAllocationResponseDTO.builder().allocationId(allocation.getAllocationId())
					.programId(allocation.getProgramId()).amount(allocation.getAmount()).date(allocation.getDate())
					.status(allocation.getStatus().name()).build());
		}

		return responseList;
	}

	@Override
	public BudgetSummaryDTO getBudgetSummary(Long programId) {

		logger.info("Generating budget summary for Program ID: {}", programId);

		FinancialProgramResponseDTO program;

		try {
			// ✅ MICROservice-safe: check Program exists via Feign
			program = financialProgramClient.getProgramById(programId);
		} catch (RuntimeException ex) {
			// ✅ Translate remote failure into domain exception
			throw new ProgramNotFound("Program not found with id: " + programId);
		}

		BigDecimal baseBudget = BigDecimal.valueOf(program.getBudget());

		BigDecimal totalAllocated = budgetAllocationRepository.getTotalAllocatedByProgramId(programId);

		BigDecimal remainingBase = baseBudget.subtract(totalAllocated);

		// ✅ MICROservice-safe: used amount via Feign
		BigDecimal totalUsed;
		try {
			totalUsed = financialProgramClient.getApprovedAmount(programId);
		} catch (RuntimeException ex) {
			throw new ProgramNotFound("Unable to fetch approved amount for Program ID: " + programId);
		}

		BigDecimal remainingAllocated = totalAllocated.subtract(totalUsed);

		logger.info("Budget summary calculated | Program ID {} | Base: {}, Allocated: {}, Used: {}", programId,
				baseBudget, totalAllocated, totalUsed);

		return BudgetSummaryDTO.builder().programId(programId).baseBudget(baseBudget).totalAllocated(totalAllocated)
				.remainingBase(remainingBase).totalUsed(totalUsed).remainingAllocated(remainingAllocated).build();
	}

	@Override
	public String deleteAllocation(Long allocationId) {

		logger.info("Deleting budget allocation with ID: {}", allocationId);

		BudgetAllocation allocation = budgetAllocationRepository.findById(allocationId).orElseThrow(() -> {
			logger.warn("Attempt to delete non-existing allocation ID: {}", allocationId);
			return new AllocationNotFoundException("Budget allocation not found");
		});

		budgetAllocationRepository.delete(allocation);

		logger.info("Budget allocation deleted successfully. ID: {}", allocationId);

		return "Budget allocation deleted successfully";
	}
}