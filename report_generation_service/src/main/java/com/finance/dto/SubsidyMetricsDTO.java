package com.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubsidyMetricsDTO {
	private Double totalBudgetAllocated;
	private Double amountDisbursed;
	private Long totalApplications;
	private Long pendingValidations;
}