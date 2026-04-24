package com.finance.dto;

import com.finance.enums.AuditStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditUpdateRequest {

	@Size(max = 2000, message = "{audit.findings.size}")
	private String findings;

	@NotNull(message = "{audit.status.notNull}")
	private AuditStatus status;
}