package com.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BudgetAllocationResponseDTO {
	private Long allocationId;
	private Long programId;
	private BigDecimal amount;
	private LocalDate date;
	private String status;
}