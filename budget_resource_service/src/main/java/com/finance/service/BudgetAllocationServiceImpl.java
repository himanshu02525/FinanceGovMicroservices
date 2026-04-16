package com.financegov.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.financegov.dto.BudgetAllocationRequestDTO;
import com.financegov.dto.BudgetAllocationResponseDTO;
import com.financegov.dto.BudgetSummaryDTO;
import com.financegov.enums.AllocationStatus;
import com.financegov.enums.ProgramStatus;
import com.financegov.exceptions.AllocationNotFoundException;
import com.financegov.exceptions.InvalidAllocationStatusException;
import com.financegov.exceptions.ProgramNotFound;
import com.financegov.model.BudgetAllocation;
import com.financegov.model.FinancialProgram;
import com.financegov.repository.BudgetAllocationRepository;
import com.financegov.repository.FinancialProgramRepository;
import com.financegov.repository.SubsidyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BudgetAllocationServiceImpl implements BudgetAllocationService {

	private static final Logger logger = LoggerFactory.getLogger(BudgetAllocationServiceImpl.class);

	private final BudgetAllocationRepository budgetAllocationRepository;
	private final FinancialProgramRepository financialProgramRepository;
	private final SubsidyRepository subsidyRepository;

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

		FinancialProgram program = financialProgramRepository.findById(dto.getProgramId()).orElseThrow(() -> {
			logger.warn("Program not found for ID: {}", dto.getProgramId());
			return new ProgramNotFound("Program not found, cannot allocate budget");
		});

		if (program.getStatus() != ProgramStatus.ACTIVE) {
		    logger.warn("Budget allocation attempt for Program ID {} failed. Current status: {}",
		            program.getProgramId(), program.getStatus());
		    throw new IllegalStateException(
		            "Budget can be allocated only when the program status is ACTIVE");
		}
	
		
		BudgetAllocation allocation = BudgetAllocation.builder().program(program).amount(dto.getAmount())
				.date(dto.getDate()).status(mapStatus(dto.getStatus())).build();

		BudgetAllocation saved = budgetAllocationRepository.save(allocation);

		logger.info("Budget allocation created successfully. Allocation ID: {}, Amount: {}", saved.getAllocationId(),
				saved.getAmount());

		return BudgetAllocationResponseDTO.builder().allocationId(saved.getAllocationId())
				.programId(saved.getProgram().getProgramId()).amount(saved.getAmount()).date(saved.getDate())
				.status(saved.getStatus().name()).build();
	}

	@Override
	public List<BudgetAllocationResponseDTO> getAllAllocations() {

		logger.info("Fetching all budget allocations");

		List<BudgetAllocationResponseDTO> responseList = new ArrayList<>();

		List<BudgetAllocation> allocations = budgetAllocationRepository.findAll();

		logger.debug("Total allocations found: {}", allocations.size());

		for (BudgetAllocation allocation : allocations) {
			responseList.add(BudgetAllocationResponseDTO.builder().allocationId(allocation.getAllocationId())
					.programId(allocation.getProgram().getProgramId()).amount(allocation.getAmount())
					.date(allocation.getDate()).status(allocation.getStatus().name()).build());
		}

		return responseList;
	}

	@Override
	public BudgetSummaryDTO getBudgetSummary(Long programId) {

		logger.info("Generating budget summary for Program ID: {}", programId);

		FinancialProgram program = financialProgramRepository.findById(programId).orElseThrow(() -> {
			logger.warn("Budget summary requested for invalid Program ID: {}", programId);
			return new ProgramNotFound("Program not found");
		});

		BigDecimal baseBudget = BigDecimal.valueOf(program.getBudget());

		BigDecimal totalAllocated = budgetAllocationRepository.getTotalAllocatedByProgramId(programId);

		BigDecimal remainingBase = baseBudget.subtract(totalAllocated);

		BigDecimal totalUsed = subsidyRepository.sumApprovedAmountByProgramId(programId);

		BigDecimal remainingAllocated = totalAllocated.subtract(totalUsed);

		logger.info("Budget summary calculated for Program ID {} | Base: {}, Allocated: {}, Used: {}", programId,
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