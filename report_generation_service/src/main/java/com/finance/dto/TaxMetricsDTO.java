package com.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxMetricsDTO {
	private Double totalTaxCollected;
	private Long activeTaxPayers;
	private Integer pendingDisclosures;
}
