package com.finance.model;

import java.time.LocalDateTime;

import com.finance.enums.ReportScope;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reportId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReportScope scope;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String metrics;

	@Column(nullable = false)
	private LocalDateTime generatedDate;
}