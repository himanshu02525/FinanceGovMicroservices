package com.finance.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.finance.enums.ComplianceRecordResult;
import com.finance.enums.ComplianceRecordType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ComplianceRecord")
public class ComplianceRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ComplianceID", nullable = false)
	private long complianceId;

	@NotNull(message = "{compliance.entityId.notNull}")
	@Positive(message = "{compliance.entityId.positive}")
	@Column(name = "EntityID", nullable = false, updatable = false)
	private long entityId; 

	@NotNull(message = "{compliance.referenceID.notNull}")
	@Positive(message = "{compliance.referenceID.positive}")
	@Column(name = "ReferenceID", nullable = false, updatable = false)
	private long referenceID;

	@NotNull(message = "{compliance.type.notNull}")
	@Enumerated(EnumType.STRING)
	@Column(name = "Type", nullable = false, length = 20, updatable = false)
	private ComplianceRecordType type;

	@NotNull(message = "{compliance.result.notNull}")
	@Enumerated(EnumType.STRING)
	@Column(name = "Result", nullable = false, length = 20)
	private ComplianceRecordResult result = ComplianceRecordResult.PENDING;

	@Size(max = 1000, message = "{compliance.notes.size}")
	@Column(name = "Notes", length = 1000)
	private String notes;

	@CreationTimestamp
	@Column(name = "CreatedAt", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PastOrPresent(message = "{compliance.date.pastOrPresent}")
	@Column(name = "ClosedAt")
	private LocalDateTime closedAt;
}