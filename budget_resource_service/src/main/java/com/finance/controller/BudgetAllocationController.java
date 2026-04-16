
package com.financegov.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financegov.dto.BudgetAllocationRequestDTO;
import com.financegov.dto.BudgetAllocationResponseDTO;
import com.financegov.dto.BudgetSummaryDTO;
import com.financegov.service.BudgetAllocationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/budget-allocations")
@RequiredArgsConstructor
public class BudgetAllocationController {

	private final BudgetAllocationService budgetAllocationService;

	@PostMapping("/createAllocation")
	public ResponseEntity<BudgetAllocationResponseDTO> createAllocation(@Valid @RequestBody BudgetAllocationRequestDTO dto) 
	{
		return new ResponseEntity<>(budgetAllocationService.createAllocation(dto), HttpStatus.CREATED);
	}
     
	@GetMapping("/getAllAllocation")
	public ResponseEntity<List<BudgetAllocationResponseDTO>> getAllAllocations() 
	{
		return ResponseEntity.ok(budgetAllocationService.getAllAllocations());
	}

	@GetMapping("/summary/{programId}")
	public ResponseEntity<BudgetSummaryDTO> getBudgetSummary(@PathVariable Long programId) 
	{
		return ResponseEntity.ok(budgetAllocationService.getBudgetSummary(programId));
	}

	@DeleteMapping("/deleteAllocation/{allocationId}")
	public ResponseEntity<Map<String, String>> deleteAllocation(@PathVariable Long allocationId) 
	{
		Map<String, String> response = new HashMap<>();
		response.put("message", budgetAllocationService.deleteAllocation(allocationId));
		return ResponseEntity.ok(response);
	}
}
