package com.finance.dto;

import java.time.LocalDateTime;

import com.finance.enums.ReportScope;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
	private Long reportId;
	private ReportScope scope;
	private Object metrics;
	private LocalDateTime generatedDate;
}
