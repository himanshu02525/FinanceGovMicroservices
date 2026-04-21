package com.finance.model;

import java.math.BigDecimal;

import com.finance.enums.TaxStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tax_records") // Maps to the tax_records table in the microservice database [cite: 94, 104]
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaxRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taxId; // Unique identifier for the tax record [cite: 58, 104]

    @NotNull(message = "Entity ID is required")
    @Column(name = "entity_id", nullable = false)
    private Long entityId; // Replaces CitizenBusiness object to decouple from other modules [cite: 104]

    @NotNull(message = "Fiscal year is required")
    @Min(value = 2000, message = "System does not support records before year 2000")
    @Column(nullable = false)
    private Integer year; // The fiscal year for which the tax is filed [cite: 58, 104]

    @NotNull(message = "Amount is required")
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount; // The tax amount filed by the citizen/business [cite: 58, 104]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TaxStatus status; // Current status of the tax record (e.g., PENDING, PAID) [cite: 58, 104]
}