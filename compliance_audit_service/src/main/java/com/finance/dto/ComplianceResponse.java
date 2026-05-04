package com.finance.dto;

import java.time.LocalDateTime;

import com.finance.enums.ComplianceRecordResult;
import com.finance.enums.ComplianceRecordType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
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