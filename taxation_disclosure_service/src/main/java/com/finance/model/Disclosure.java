package com.finance.model;

import java.time.LocalDateTime;
import com.finance.enums.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "disclosures")
@Data
@NoArgsConstructor
public class Disclosure {

	// Unique identifier for the disclosure record
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long disclosureId;

	// Logical reference to the Entity ID from the Citizen/Business microservice
	@NotNull(message = "Entity ID is required")
	@Column(name = "entity_id", nullable = false)
	private Long entityId;

	// Type of disclosure: INCOME or EXPENSE [cite: 59, 105]
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private DisclosureType type;

	// Workflow status (e.g., SUBMITTED, VALIDATED) [cite: 59, 105]
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 15)
	private DisclosureStatus status;

	// Timestamp of when the disclosure was filed [cite: 59]
	@Column(nullable = false, updatable = false)
	private LocalDateTime submissionDate = LocalDateTime.now();
}