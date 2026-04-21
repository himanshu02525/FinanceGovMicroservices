package com.finance.dto;

import com.finance.enums.ComplianceRecordResult;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplianceUpdateRequest {
	@NotNull(message = "{compliance.result.notNull}")
	private ComplianceRecordResult result;

	@Size(max = 1000, message = "{compliance.notes.size}")
	private String notes;
}