package com.finance.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.enums.ComplianceRecordResult;
import com.finance.enums.ComplianceRecordType;

import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ComplianceResponse {

	private Long complianceId;
	private Long referenceId;
	private Long entityId;

	private ComplianceRecordType type;

	private ComplianceRecordResult result;

	private LocalDateTime createdAt;

	private LocalDateTime closedAt;

	private String notes;
	private TaxResponseDTO taxResponseDTO;
	private SubsidyResponse subsidyResponse;
	private FinancialProgramResponse financialProgramResponse;

}
