package com.finance.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;
import com.finance.enums.SubsidyStatus;

@Entity
@Table(name = "subsidy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subsidy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subsidyId;

    // External reference to Citizen service
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    private Double amount;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private SubsidyStatus status;

    // Internal reference to FinancialProgram (your own module)
    @ManyToOne
    @JoinColumn(name = "program_id", nullable = false)
    private FinancialProgram program;
}
