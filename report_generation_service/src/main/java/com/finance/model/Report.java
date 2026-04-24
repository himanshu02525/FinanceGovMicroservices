package com.finance.model;

import java.time.LocalDateTime;

import com.finance.enums.ReportScope;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Enumerated(EnumType.STRING)
    private ReportScope scope;

    // -------- PROGRAM --------
    private Integer totalPrograms;
    private Integer activePrograms;
    private Double budgetUsed;

    // -------- SUBSIDY --------
    private Integer applicationsReceived;
    private Integer approvedSubsidies;
    private Double amountDistributed;

    // -------- TAX --------
    private Integer totalTaxpayers;
    private Double revenueCollected;

    private LocalDateTime generatedDate;
}