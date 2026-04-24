package com.finance.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.finance.enums.AuditScope;
import com.finance.enums.AuditStatus;

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
@Table(name = "Audit")
public class Audit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "AuditID", updatable = false, nullable = false)
	private Long auditId;

	@NotNull(message = "{audit.officerId.notNull}")
	@Positive(message = "{audit.officerId.positive}")
	@Column(name = "OfficerID", nullable = false, updatable = false)
	private Long officerId;

	@NotNull(message = "{audit.scope.notNull}")
	@Enumerated(EnumType.STRING)
	@Column(name = "Scope", nullable = false, length = 50, updatable = false)
	private AuditScope scope;

	@Size(max = 2000, message = "{audit.findings.size}")
	@Column(name = "Findings", length = 2000)
	private String findings;

	@NotNull(message = "{audit.status.notNull}")
	@Enumerated(EnumType.STRING)
	@Column(name = "Status", nullable = false)
	private AuditStatus status = AuditStatus.PENDING;

	@CreationTimestamp
	@Column(name = "CreatedAt", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PastOrPresent(message = "{audit.closedAt.pastOrPresent}")
	@Column(name = "ClosedAt")
	private LocalDateTime closedAt;
}