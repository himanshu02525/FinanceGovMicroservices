package com.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportAnalyticsDTO {

    // PROGRAM analytics
    private Integer totalPrograms;
    private Integer activePrograms;
    private Double budgetUsed;
    private Double programUtilizationPercent;

    // SUBSIDY analytics
    private Integer applicationsReceived;
    private Integer approvedSubsidies;
    private Double approvalRatePercent;
    private Double averageSubsidyAmount;

    // TAX analytics
    private Integer totalTaxpayers;
    private Double revenueCollected;
    private Double averageRevenuePerTaxpayer;
}