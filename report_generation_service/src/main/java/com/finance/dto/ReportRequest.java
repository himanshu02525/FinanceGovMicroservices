package com.finance.dto;

import com.finance.enums.ReportScope;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ReportRequest {
	private Long programId;
	@NotNull
	private ReportScope scope;
}