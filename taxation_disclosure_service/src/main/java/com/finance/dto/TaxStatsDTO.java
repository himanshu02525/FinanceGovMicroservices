package com.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for carrying aggregated tax metrics to the administrative dashboard.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxStatsDTO {

    // The total number of unique citizens or businesses that have filed taxes
    private long totalTaxPayers;

    // The sum total of all tax payments collected across the system
    private Double totalRevenue;

}