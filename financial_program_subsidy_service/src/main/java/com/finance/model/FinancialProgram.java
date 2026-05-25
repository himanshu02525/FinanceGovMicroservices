package com.finance.model;

import java.time.LocalDate;

import com.finance.enums.ProgramStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "financial_program")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long programId;

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @Lob
    @NotBlank(message = "Description cannot be blank")
    private String description;

    @Min(value = 0, message = "Budget must be non-negative")
    @NotNull(message = "Budget is required")
    private Double budget;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "End date must be in the future")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Program status is required")
    private ProgramStatus status;
}
