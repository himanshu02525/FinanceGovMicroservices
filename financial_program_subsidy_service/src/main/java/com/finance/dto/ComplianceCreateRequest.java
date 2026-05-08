package com.finance.dto;

import com.finance.enums.ComplianceRecordType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ComplianceCreateRequest {

	@NotNull(message = "{compliance.entityId.notNull}")
	@Positive(message = "{compliance.entityId.positive}")
	private Long entityId;

	@NotNull(message = "{compliance.referenceID.notNull}")
	@Positive(message = "{compliance.referenceID.positive}")
	private Long referenceId;

	@NotNull(message = "{compliance.type.notNull}")
	private ComplianceRecordType type;

	@Size(max = 1000, message = "{compliance.notes.size}")
	private String notes;
}