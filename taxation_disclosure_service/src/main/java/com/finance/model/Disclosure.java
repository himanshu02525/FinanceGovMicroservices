package com.finance.model;

import java.time.LocalDateTime;
import com.finance.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "disclosures") // Maps to the disclosures table in the microservice database [cite: 94, 105]
@Data
@NoArgsConstructor
public class Disclosure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long disclosureId; // Unique identifier for the disclosure record [cite: 59, 105]

    @NotNull(message = "Entity ID is required")
    @Column(name = "entity_id", nullable = false)
    private Long entityId; // Logical reference to the Entity ID from the Citizen/Business microservice [cite: 105]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DisclosureType type; // Type of disclosure: INCOME or EXPENSE [cite: 59, 105]

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DisclosureStatus status; // Workflow status (e.g., SUBMITTED, VALIDATED) [cite: 59, 105]

    @Column(nullable = false, updatable = false)
    private LocalDateTime submissionDate = LocalDateTime.now(); // Timestamp of when the disclosure was filed [cite: 59]
}