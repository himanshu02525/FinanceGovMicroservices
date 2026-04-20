package com.finance.dto;

import com.finance.enums.AuditScope;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditCreateRequest {

	@NotNull(message = "{audit.officerId.notNull}")
	@Positive(message = "{audit.officerId.positive}")
	private Long officerId;

	@NotNull(message = "{audit.scope.notNull}")
	private AuditScope scope;

	@Size(max = 2000, message = "{audit.findings.size}")
	private String findings;
}