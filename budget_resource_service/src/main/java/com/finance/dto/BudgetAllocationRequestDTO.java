package com.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BudgetAllocationRequestDTO {
	private Long programId;
	private BigDecimal amount;
	private LocalDate date;
	private String status;
}

