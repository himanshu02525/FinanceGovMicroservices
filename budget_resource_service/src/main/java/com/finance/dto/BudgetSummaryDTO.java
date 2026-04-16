
package com.finance.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BudgetSummaryDTO {

    private Long programId;

    // ✅ Base budget from FinancialProgram (Module 3)
    private BigDecimal baseBudget;

    // ✅ Allocated budget from BudgetAllocation (Module 5)
    private BigDecimal totalAllocated;

    // ✅ Remaining base budget = baseBudget - totalAllocated
    private BigDecimal remainingBase;

    // ✅ Used budget from approved Subsidies (Module 3)
    private BigDecimal totalUsed;

    // ✅ Remaining allocated budget = totalAllocated - totalUsed
    private BigDecimal remainingAllocated;
}
