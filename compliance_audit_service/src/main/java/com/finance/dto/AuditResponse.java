package com.finance.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.enums.AuditScope;
import com.finance.enums.AuditStatus;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AuditResponse {
	private Long auditId;
	private Long officerId;
	private AuditScope scope;
	private String findings;
	private AuditStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime closedAt;
	private TaxResponseDTO tax;
	private SubsidyResponse subsidyResponse;
	private FinancialProgramResponse financialProgramResponse;
}
