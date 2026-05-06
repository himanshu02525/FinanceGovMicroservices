package com.finance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.finance.enums.TaxStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaxResponseDTO {
	private Long taxId;
	private Long entityId;
	private Integer year;
	private BigDecimal amount;
	private TaxStatus status;
	private LocalDateTime createdAt;
}