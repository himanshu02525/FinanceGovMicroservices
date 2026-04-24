package com.finance.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.finance.enums.ApplicationStatus;

@Entity
@Table(name = "subsidy_application")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubsidyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;   // ✅ this is the field Spring Data will use
    
    
    @NotNull(message = "Submitted date is required")
    @PastOrPresent(message = "Submitted date cannot be in the future")
    private LocalDate submittedDate;

    @ManyToOne
    @JoinColumn(name = "program_id", nullable = false)
    private FinancialProgram program;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Application status is required")
    private ApplicationStatus status;
}
