package com.finance.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TaxRequestDTO {
    @NotNull(message = "Entity ID is required")
    private Long entityId; // The ID of the citizen or business filing the tax

    @NotNull(message = "Fiscal year is required")
    @Min(value = 2000, message = "Year must be at least 2000")
    private Integer year; // The year for which taxes are being filed

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount cannot be negative")
    private BigDecimal amount; // The total taxable amount reported
}