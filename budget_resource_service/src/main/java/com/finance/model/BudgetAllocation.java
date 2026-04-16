package com.finance.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.finance.enums.AllocationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "BudgetAllocation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AllocationID")
    private Long allocationId;

    // ✅ Microservice-safe: only programId (no relationship)
    @NotNull(message = "Program is required")
    @Column(name = "ProgramID", nullable = false)
    private Long programId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(name = "Amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Date cannot be in past")
    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private AllocationStatus status;

    // ✅ NEW: Automatically set today's date if not provided
    @PrePersist
    private void setDefaultDate() {
        if (this.date == null) {
            this.date = LocalDate.now();
        }
    }
}
